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
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

            // 通过组装SearchRequest来进行查询
            SearchResponse response = restHighLevelClient.search(coverSearchRequest(indexName, sourceBuilder), RequestOptions.DEFAULT);

            if (null == response) {
                logger.info("search is null,params : " + inputMap);
                return searchResult;
            }

            SearchHit[] searchHits = coverHighlight(response, highlightFieldList).getHits().getHits();

            for (SearchHit hit : searchHits) {
                // 此处需使用getSourceAsMap，否则无法获取到高亮信息
//                searchResult.add(hit.getSourceAsString());
                searchResult.add(hit.getSourceAsMap());
//                logger.info(JSONUtils.toString(hit.getSourceAsMap()));
            }

        } catch (Throwable e) {
            logger.error("searchDataByFilter is Exception,Message = " + e.getMessage(), e);
        }

        logger.info("searchResult size : " + searchResult.size());


        return searchResult;
    }

    /**
     * 组装searchRequest对象，主要用于设置索引、routing、sourceBuilder等参数
     *
     * @param indexName
     * @param sourceBuilder
     * @return
     */
    private SearchRequest coverSearchRequest(String indexName, SearchSourceBuilder sourceBuilder) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    /**
     * 组装SearchSourceBuilder对象，主要用于设置分页、搜索条件、高亮字体等
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
         * termQuery("key", obj)                完全匹配
         * termsQuery("key", obj1, obj2..)      一次匹配多个值
         * matchQuery("key", Obj)               单个匹配，key field 不支持通配符, 前缀具高级特性
         * matchAllQuery();                     匹配所有文件
         * multiMatchQuery("text", "field1", "field2"..)      匹配多个字段, key field 有通配符也行
         */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {

            if (entry.getValue() instanceof String) {
                boolQueryBuilder.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
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
        sourceBuilder.query(boolQueryBuilder);

        // 当前第几页（暂时不支持分页，只展示指定条数）
        sourceBuilder.from(pageIndex);
        // 设置展示多少条数据
        sourceBuilder.size(pageSize);

        // 设置高亮展示,requireFieldMatch:如果多字段需要高亮，则该值必须为false，field("*")：表示需要高亮的字段（此处不做限制，使用*）
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*");//.requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
//        highlightBuilder.fragmentSize(80000); //最大高亮分片数
//        highlightBuilder.numOfFragments(0); //从第一个分片获取高亮片段
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
        //遍历结果
        for (SearchHit hit : response.getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            //处理高亮片段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            for (String highlightField : highlightFieldList) {
                HighlightField nameField = highlightFields.get(highlightField);
                if (nameField != null) {
                    Text[] fragments = nameField.fragments();
                    StringBuilder nameTmp = new StringBuilder();
                    for (Text text : fragments) {
                        nameTmp.append(text);
                    }
                    //将高亮片段组装到结果中去
                    source.put(highlightField, nameTmp.toString());
//                    logger.info(source.toString());
                }
            }
        }
        return response;
    }


    /**
     * 根据索引名及ID获取文档内容
     *
     * @param indexName
     * @param id
     * @return
     * @throws IOException
     */
    public String searchDataById(String indexName, String id) throws IOException {

        //GetRequest()方法第一个参数是索引的名字,第二个参数是文档的id
        GetRequest getRequest = new GetRequest(indexName, id);

        GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

        String resultDoc = "";
        if (getResponse.isExists()) {
            resultDoc = getResponse.getSourceAsString();
        }

        return resultDoc;

    }

}
