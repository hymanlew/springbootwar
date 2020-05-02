package com.hyman.springbootwar.esconfig;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
public class EsCrud2 {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 添加批量增加文档（如果ID已存在ES中，则会覆盖操作）
     *
     * @param inputMap  --> key : id ； value : id对应的json串
     * @throws IOException
     */
    public void addBatchData(String indexName, Map<String, String> inputMap) throws IOException {

        BulkRequest request = new BulkRequest();
        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            request.add(new IndexRequest(indexName).id(entry.getKey()).source(entry.getValue(), XContentType.JSON));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            log.error("addBatchData add failed,Message = " + bulkResponse.buildFailureMessage());
        }
    }


    /**
     * 批量修改ES文档
     *
     * @param inputMap  --> key : id ； value : id对应的json串
     */
    public void updateBatchData(String indexName, Map<String, String> inputMap) throws IOException {

        BulkRequest request = new BulkRequest();
        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            request.add(new UpdateRequest(indexName, entry.getKey()).doc(entry.getValue(), XContentType.JSON));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            log.error("updateBatchData Update failed,Message = " + bulkResponse.buildFailureMessage());
        }
    }

    /**
     * 删除索引及ID对应的数据
     */
    public void deleteData(String indexName, String id) throws IOException {

        //索引   以及id
        DeleteRequest request = new DeleteRequest(indexName, id);
        DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);

        //异步删除
        //client.deleteAsync(request, RequestOptions.DEFAULT, listener);

        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure :
                    shardInfo.getFailures()) {
                String reason = failure.reason();
                log.error("deleteData delete failed, Message = " + reason);
            }
        }
    }

    /**
     * 批量删除ES文档
     *
     * @param indexName
     * @param ids
     * @throws IOException
     */
    public void deleteBatchData(String indexName, List<String> ids) throws IOException {

        DeleteRequest deleteRequest = new DeleteRequest(indexName);

        BulkRequest request = new BulkRequest();
        for (String id : ids) {
            request.add(deleteRequest.id(id));
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (bulkResponse.hasFailures()) {
            // process failures by iterating through each bulk response item
            log.error("deleteBatchData delete failed,Message = " + bulkResponse.buildFailureMessage());
        }
    }

    /**
     * 根据条件查询ES数据
     *
     * @param indexName
     * @param inputMap       --> key : 查询字段名 ； value : 如果为范围查询，则传入Map（map包含两个key："start" 和 "end"，分别表示范围的起始），反之则传入String
     * @param highlightFieldList 分词需要的高亮字段
     * @param pageIndex
     * @param pageSize
     * @return 返回值 ： resultMap --> key : id ； value : id对应的json串
     * @throws IOException
     */
    public List<Map<String, Object>> searchDataByFilter(String indexName, Map<String, Object> inputMap, List<String> highlightFieldList, Integer pageIndex, Integer pageSize) throws IOException {

        List<Map<String, Object>> searchResult = new ArrayList<>();
        try {

            // 组装条件等相关信息
            SearchSourceBuilder sourceBuilder = coverSearchSourceBuilder(inputMap, pageIndex, pageSize);

            // 通过组装 SearchRequest 来进行查询
            SearchResponse response = restHighLevelClient.search(coverSearchRequest(indexName, sourceBuilder), RequestOptions.DEFAULT);
            if (null == response) {
                log.info("search is null, params : " + inputMap);
                return searchResult;
            }

            // 要访问返回的文档，首先需要获取响应中包含的 SearchHits。它指的是搜索到的命中次数，并提供了所有命中的全局信息，例如索引，文档ID
            // 和每个搜索命中的分数。
            SearchHit[] searchHits = coverHighlight(response, highlightFieldList).getHits().getHits();
            for (SearchHit hit : searchHits) {
                /**
                 * 在此映射中，常规字段由字段名称 + 字段值。多值字段作为对象列表返回，嵌套对象作为另一个键/值映射返回。这些情况需要相应地强制
                 * 转换。
                 * 此处需使用 getSourceAsMap，否则无法获取到高亮信息
                 */
                //searchResult.add(hit.getSourceAsString());
                searchResult.add(hit.getSourceAsMap());
            }

        } catch (Throwable e) {
            log.error("searchDataByFilter is Exception,Message = " + e.getMessage(), e);
        }

        log.info("searchResult size : " + searchResult.size());
        return searchResult;
    }

    /**
     * 组装 searchRequest 对象，主要用于设置索引、routing、sourceBuilder 等参数
     *
     * @param indexName
     * @param sourceBuilder
     * @return
     */
    private SearchRequest coverSearchRequest(String indexName, SearchSourceBuilder sourceBuilder) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // 该参数的作用是，控制并解决不可用的索引以及如何扩展通配符表达式
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());

        // 指定优先搜索的分片（以下设置为本地），默认设置是随机分片。
        searchRequest.preference("_local");
        return searchRequest;
    }

    /**
     * 组装 SearchSourceBuilder 对象，主要用于设置分页、搜索条件、高亮字体等
     *
     * @param inputMap
     * @param pageIndex
     * @param pageSize
     * @return
     */
    private SearchSourceBuilder coverSearchSourceBuilder(Map<String, Object> inputMap, Integer pageIndex, Integer pageSize) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        /**
         * 使用 QueryBuilder：
         * termQuery("key", obj)                完全匹配，词条查询
         * termsQuery("key", obj1, obj2..)      一次匹配多个值
         * matchQuery("key", Obj)               单个匹配，key 为字段值，obj 为 text 查询文本，不支持通配符, 前缀等高级特性。
         * matchAllQuery();                     匹配所有文件，文档
         * queryStringQuery("text")             对所有字段，进行分词 text 查询
         * wildcardQuery("key", "obj*")         通配符查询（* 表示多个任意的字符，？ 表示单个字符）
         * multiMatchQuery("text", "field1", "field2"..)      匹配多个字段, key field 有通配符也行
         * regexpQuery(String name, String regexp)            将包含术语的文档与指定的正则表达式匹配的查询
         *
         * queryStringQuery(String queryString)               解析查询字符串并运行它的查询。有两种模式。
         * 第一，当没有字段添加时（使用 QueryStringQueryBuilder.field(字符串)），将运行查询一次,非前缀字段将使用 QueryStringQueryBuilder.defaultField(字符串)。
         * 第二，当一个或多个字段添加时（如上）,将运行提供的解析查询字段，并结合使用 DisMax 或普通的布尔查询（参见QueryStringQueryBuilder.useDisMax(布尔)）。
         *
         * wrapperQuery(String source)：
         * 一个查询构建器，它允许构建给定 JSON 字符串或二进制数据作为输入的查询。当使用 Java Builder API 时，仍然需要将 JSON 查
         * 询字符串与其他查询构建器结合时，这是非常有用的。
         *
         * sourceBuilder.query(QueryBuilders.queryStringQuery("搜索内容"));
         * sourceBuilder.query(QueryBuilders.wildcardQuery("context", "搜索内容*"));
         *
         * fuzzyquery：
         * 是用编辑距离度量俩词项的相似度，将所有相似的词项填充成布尔查询(或)来查询。它还有两个构造函数，来限制模糊匹配的程度。
         * 在 FuzzyQuery 中，默认的匹配度是0.5，当这个值越小时，通过模糊查找出的文档的匹配程度就越低，查出的文档量就越多,反之亦然。
         * 是取所有相同前缀（前缀长度可以设定）的词项做编辑距离。
         *
         * wildcardquery：
         * 而这个是通配符查询，使用的通用查询。
         *
         * 间隔查询（Intervals queries）：
         * 某些搜索用例（例如法律和专利搜索）引入了查找单词或短语彼此相距一定距离的记录的搜索规则。Elasticsearch 7.0中的间隔查询
         * 引入了一种构建此类查询的全新方式，与之前的方法（跨度查询span queries）相比，使用和定义更加简单，且间隔查询对边缘情况的
         * 适应性更强。
         *
         * 注意，通配符查询可能很慢，因为它需要遍历许多项。为了防止异常缓慢的通配符查询，通配符项不应该以一个通配符 * 或 ? 开头。
         *
         *
         * filter 与 query 还是有很大的区别的：
         * 1，比如 query 的时候，会先比较查询条件，然后计算分值，最后返回文档结果。
         * 2，而 filter 则是先判断是否满足查询条件，如果不满足，会缓存查询过程（记录该文档不满足结果）。满足的话，就直接缓存结果。
         *
         * 综上所述，filter 快在两个方面：
         * 1，对结果进行缓存
         * 2，避免计算分值
         *
         *
         * bool 查询的使用：
         * Bool 查询对应 Lucene 中的 BooleanQuery，它由一个或者多个子句组成，每个子句都有特定的类型。
         *
         * must，返回的文档必须满足 must 子句的条件，并且参与计算分值。
         * filter，返回的文档必须满足 filter 子句的条件，但是不参与计算分值。
         * should，返回的文档可能满足 should 子句的条件。在一个bool查询中，如果没有 must 或者 filter，有一个或者多个 should
         *         子句。那么只要满足一个就可以返回。minimum_should_match 参数定义了至少满足几个子句。
         * must_not，返回的文档必须不满足定义的条件。
         *
         * 如果一个查询既有 filter 又有 should，那么至少要包含一个 should 子句。
         * bool 查询也支持，禁用协同计分选项 disable_coord。一般计算分值的因素取决于所有的查询条件。
         * bool 查询也是采用 more_matches_is_better 的机制，因此满足 must 和 should 子句的文档将会合并起来计算分值。
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {

            if (entry.getValue() instanceof String) {
                boolQueryBuilder.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));

            } else if (entry.getValue() instanceof Map) {
                // 使用区域查询
                Map<String, String> map = (Map<String, String>) entry.getValue();
                RangeQueryBuilder rqb = QueryBuilders.rangeQuery(entry.getKey());

                String start = map.get("start");
                String end = map.get("end");
                if (!StringUtils.isEmpty(start)) {
                    rqb.from(start);
                }
                if (!StringUtils.isEmpty(end)) {
                    rqb.to(end);
                }
                boolQueryBuilder.must(rqb);
            }
        }

        sourceBuilder.query(QueryBuilders.fuzzyQuery("content", "hello*"));
        sourceBuilder.query(boolQueryBuilder);

        /**
         * 分页 & 遍历
         * From：指定开始的位置。Size：指定期望获取文档的总数。
         * ES天生就是分布式系统，查询信息，但是数据分别保存在多个分片中，多台机器上，ES天生就需要满足排序的需求（按照相关性算分）。
         *
         * 当执行一个查询：From=990, Size=10 时，会在每个分片中获取1000个文档。然后在通过 Coordinating Node 聚合所有结果。最好
         * 再通过排序选取前1000个文档页数越深，占用内存越多。为了避免深度分页带来的内存开销，ES 有一个设定，默认限定10000个文档。
         */
        // 设置确定开始搜索的结果索引的 from 选项。 默认为0。
        sourceBuilder.from(pageIndex);
        // 设置大小选项，该选项确定要返回的搜索命中数。 默认为10。设置展示多少条数据
        sourceBuilder.size(pageSize);
        // 设置一个可选的超时时间，以控制允许搜索的时间。
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 按照 _score 字段倒序排序（这也是默认的方式）。
        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        // 按照 _id 字段正序排序
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

        /**
         * 设置高亮展示：
         * 通过将一个或多个 HighlightBuilder.Field 实例添加到 HighlightBuilder 中，可以为每个字段定义不同的突出显示行为。
         *
         * requireFieldMatch(false)，如果多字段需要高亮，则该值必须为 false。
         * field("*")，表示设置需要高亮的字段（此处不做限制，使用*）。
         */
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*");
        //highlightBuilder.requireFieldMatch(false);

        // 设置显示的格式，样式
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");

        // 下面两项，表示如果你要高亮如文字内容等有很多字的字段，则必须配置。不然会导致高亮不全，文章内容缺失等。
        // 最大高亮分片数
        highlightBuilder.fragmentSize(80000);
        // 从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(0);

        sourceBuilder.highlighter(highlightBuilder);
        return sourceBuilder;
    }

    /**
     * 设置高亮，将高亮片段拼接到结果中
     *
     * @param response
     * @return
     */
    private SearchResponse coverHighlight(SearchResponse response, List<String> highlightFieldList) {

        // 遍历结果
        for (SearchHit hit : response.getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            // 处理高亮片段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            for (String highlightField : highlightFieldList) {

                HighlightField nameField = highlightFields.get(highlightField);
                if (nameField != null) {
                    // 获取所有的高亮的字段文本的片段
                    Text[] fragments = nameField.fragments();
                    StringBuilder nameTmp = new StringBuilder();
                    for (Text text : fragments) {
                        nameTmp.append(text);
                    }

                    // 将高亮片段组装到结果中去
                    source.put(highlightField, nameTmp.toString());
                    log.info(source.toString());
                }
            }
        }
        return response;
    }

}
