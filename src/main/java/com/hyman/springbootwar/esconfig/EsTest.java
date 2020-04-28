package com.hyman.springbootwar.esconfig;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "es")
public class EsTest {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 索引名称
     */
    private String indexName = "indexname";

    @RequestMapping(value = "hello")
    public String hello() {

        Map<String, Object> query = new HashMap<>();
        try {

            // 注意 indexId 是唯一的
            Goods goods = new Goods(1L,"Gary1", "gggggggggggggggggggggggggg");
            String indexId = "test001";
            add(goods, indexId);

            goods = new Goods(2L,"Gary2", "kkkkkkkkkkkkkkkkkkkk");
            indexId = "test002";
            add(goods, indexId);

            query = this.query("test002");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Hello World!" + query.toString();
    }

    /**
     * RestHighLevelClient 中的所有 API 都接受一个 RequestOptions，可以使用它们来自定义请求（IndexRequest）。
     */
    private boolean add(Goods goods, String indexId) throws IOException {

        String json = new Gson().toJson(goods);

        /**
         * 索引名称，document 文档 id，文档内容
         */
        IndexRequest request = new IndexRequest(indexName).id(indexId).source(json, XContentType.JSON);

        /**
         * 当以以下方式执行 IndexRequest 时，客户端在继续执行代码之前会等待 IndexResponse 返回（即它是同步执行的）。
         * 如果无法在 restHighLevelClient 中解析 REST 响应，请求超时或类似的情况（从服务器没有返回响应），则该同步调用可能会
         * 引发 IOException。
         *
         * 在服务器返回 4xx 或 5xx 错误代码的情况下，restHighLevelClient 尝试解析返回的错误信息报文，然后引发通用 ElasticsearchException，
         * 并将原始 ResponseException 作为抑制的异常添加到它。
         */
        restHighLevelClient.index(request, RequestOptions.DEFAULT);

        /**
         * 以下是以异步方式执行 IndexRequest，以便客户端可以直接返回。用户需要将请求和侦听器传递给异步索引方法，来指定如何处
         * 理响应或潜在的失败（调用 ActionListener）。
         * 异步方法不会阻塞，而是会立即返回。完成后，如果执行成功，则使用 onResponse 方法调用 ActionListener。如果执行失败，
         * 则使用 onFailure 方法调用 ActionListener。故障情况和预期的异常与同步方法相同。
         */
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                log.info("请求成功");
            }

            @Override
            public void onFailure(Exception e) {
                log.error("请求失败");
            }
        };
        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, listener);
        return true;
    }

    private boolean addMap(Map<Object, Object> map, String indexId) throws IOException {

        // 索引名称，document 文档 id，文档内容 map（以 Map 形式提供的文档源，会自动转换为 JSON 格式）
        IndexRequest request = new IndexRequest(indexName).id(indexId).source(map);
        IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);

        // 返回的 IndexResponse，允许检索有关已执行操作的信息
        String indexname = indexResponse.getIndex();
        String documentid = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {

            // 处理（如果需要）首次创建文档的情况

        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {

            // 处理（如果需要）已存在的文档被重写的情况
        }

        // 分片信息
        ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

            // 处理成功分片数量少于总分片数量的情况
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {

                // 处理潜在的故障
                String reason = failure.reason();
            }
        }
        return true;
    }

    private boolean addOther(){

        // 作为对象密钥对提供的文档源，转换为JSON格式
        IndexRequest indexRequest = new IndexRequest("posts")
                .id("1")
                .source("user", "kimchy",
                        "postDate", new Date(),
                        "message", "trying out Elasticsearch")
                .opType(DocWriteRequest.OpType.CREATE);

        indexRequest.routing("routing");
        indexRequest.timeout("1s");
        return true;
    }


    private Map<String, Object> query(String indexId) throws IOException {

        GetRequest request = new GetRequest(indexName, indexId);

        // 关闭对返回的文档进行搜索操作。默认是开启的
        request.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        // 在对返回的文档进行搜索时，也可以自定义条件进行检索
        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        request.fetchSourceContext(fetchSourceContext);


        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        return response.getSource();
    }


}
