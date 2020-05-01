package com.hyman.springbootwar.esconfig;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.util.ObjectUtils;
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
public class EsCrud {

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


    private void creatIndex(String indexName){
        CreateIndexRequest request = new CreateIndexRequest(indexName);

        // 设置当前索引的分片数量，及副本分片的数量。
        // 每个分片本身也是一个功能完善并且独立的“索引”，这个“索引”可以被放置到集群中的任何节点上。它们起到了故障转移的机制。
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );

        // mapping 映射，可以指定 json 字符串，也可以拼接为嵌套的 map。
        request.mapping(
                "{\n" +
                        "  \"properties\": {\n" +
                        "    \"message\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                XContentType.JSON);

        try {
            /**
             * 大括号唯一的用处就是在一个方法里面划分作用域。一般如果这样写，这个方法里面就不止一对大括号，如果只有一对，加不
             * 加效果相同，如果多对，每队大括号里面的变量信息不能共用，可以有相同名称的局部变量。
             */
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.startObject("properties");
                {
                    builder.startObject("message");
                    {
                        builder.field("type", "text");
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();

            // 此时 builder 会被解析成 json 字符串
            request.mapping(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 给索引起别名。别名不仅仅可以关联一个索引，它能聚合多个索引。
         * 例如我们为索引 my_index_1 和 my_index_2 创建一个别名 my_index_alias，这样对 my_index_alias 的操作(仅限读操作)，
         * 会操作 my_index_1 和 my_index_2，类似于聚合了 my_index_1 和 my_index_2。但是不能对 my_index_alias 进行写操作，
         * 因为当有多个索引的同一个别名时 alias，不能区分到底操作的是哪一个。
         *
         * 以下是创建一个带过滤器的别名。过滤器使用 Query DSL 来定义，并且被应用到所有的搜索，计数，查询，删除和其它类似的行
         * 为（不包括插入和更新操作）。
         * 例如，可以通过索引别名的路径，插入一个 user 为 123 的文档。但是搜索时，使用别名路径就只能搜索出 user 为 123 的。
         * 而使用索引名路径，就可以搜索出其他的文档。
         * 创建一个带过滤器的别名，首先需要确保所有的字段都存在于 mapping 中（例如下面的 user 字段）。
         */
        request.alias(new Alias("index_alias").filter(QueryBuilders.termQuery("user", "123")));

        // 同步方法
        try {
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 异步方法
        ActionListener<CreateIndexResponse> listener = new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                // 是否所有节点都已确认请求
                boolean acknowledged = createIndexResponse.isAcknowledged();
                // 在超时之前是否为索引中的每个分片启动了必要数量的分片副本
                boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        restHighLevelClient.indices().createAsync(request, RequestOptions.DEFAULT, listener);

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

        /**
         * 实时性（Realtime）：
         * 默认情况下，get API 是实时的，并且不会受到索引刷新频率的影响。如果一个文档被更新了(update)，但是还没有刷新，那么
         * get API 将会发出一个刷新调用，以使文档可见。这也会使其他文档在上一次刷新可见后发生变化。如果不使用实时获取，可以将
         * realtime 设为 false。
         */
        //request.realtime(false);

        // 关闭对返回的文档进行搜索操作。默认是开启的
        request.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

        // 在对返回的文档进行搜索时，也可以自定义条件进行检索
        String[] includes = new String[]{"message", "*Date"};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
        request.fetchSourceContext(fetchSourceContext);

        // 同步方法
        try {
            boolean exists = restHighLevelClient.exists(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {

            }
        }

        ActionListener<Boolean> existslistener = new ActionListener<Boolean>() {
            @Override
            public void onResponse(Boolean exists) {
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        restHighLevelClient.existsAsync(request, RequestOptions.DEFAULT, existslistener);

        /**
         * 检查是否存在，另外一个是 existSource方法，该方法可以额外检查相关文档是否已存储。如果索引的映射，已经显示取消对文档
         * 的存储 JSON 源的支持，则此方法将为该索引中的文档返回 false。
         * 这个是同步方法，同样的还有一个异步方法（existsSourceAsync）。
         */
        restHighLevelClient.existsSource(request, RequestOptions.DEFAULT);


        /**
         * 以下是同步的方式，会等待 GetResponse 的返回。
         * 如果无法在 restHighLevelClient 中解析 REST 响应，请求超时或类似的情况（从服务器没有返回响应），则该同步调用可能会
         * 引发 IOException。
         *
         * 在服务器返回 4xx 或 5xx 错误代码的情况下，restHighLevelClient 尝试解析返回的错误信息报文，然后引发通用 ElasticsearchException，
         * 并将原始 ResponseException 作为抑制的异常添加到它。
         */
        GetResponse response = null;
        try {
            response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {

            }
        }

        /**
         * 以下是以异步方式执行 GetRequest，以便客户端可以直接返回。用户需要将请求和侦听器传递给异步索引方法，来指定如何处
         * 理响应或潜在的失败（调用 ActionListener）。
         * 异步方法不会阻塞，而是会立即返回。完成后，如果执行成功，则使用 onResponse 方法调用 ActionListener。如果执行失败，
         * 则使用 onFailure 方法调用 ActionListener。故障情况和预期的异常与同步方法相同。
         */
        ActionListener<GetResponse> listener = new ActionListener<GetResponse>() {
            @Override
            public void onResponse(GetResponse getResponse) {

                // 获取索引名称，文档 id
                String index = getResponse.getIndex();
                String id = getResponse.getId();

                /**
                 * 需要注意：
                 * 当找不到对应的文档时，尽管返回的响应为 404，但仍会返回有效的 GetResponse 而不是引发异常。只是这样的响应不
                 * 包含任何源文档，并且其 isExists 方法返回false。
                 */
                if (getResponse.isExists()) {

                    // 获取响应的版本号
                    long version = getResponse.getVersion();

                    // 以下分别是以 string，map，byte 数组，来接到文档内容
                    String sourceAsString = getResponse.getSourceAsString();
                    Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
                    byte[] sourceAsBytes = getResponse.getSourceAsBytes();
                } else {

                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        restHighLevelClient.getAsync(request, RequestOptions.DEFAULT, listener);
        return response.getSource();
    }

    private void delete(String indexId){

        /**
         * 添加，删除操作，有超时设置。但是查询没有该设置，只有一个实时性设置。
         */
        DeleteRequest request = new DeleteRequest(indexName, indexId);
        request.timeout("2s");

        /**
         * _seq_no：是严格递增的顺序号，每个文档一个，Shard 级别严格递增，保证后写入 Doc 的 _seq_no 大于先写入的 Doc 的 _seq_no。
         * 任何类型的写操作，包括 index、create、update 和 Delete，都会生成一个_seq_no。作用同 _version。
         *
         * _primary_term：也和 _seq_no 一样是一个整数，每当 Primary Shard 发生重新分配时（比如重启，Primary选举等），_primary_term
         * 都会递增 1。它主要是用来在恢复数据时，处理多个文档的 _seq_no 一样时的冲突，避免 Primary Shard 上的写入被覆盖。
         * Elasticsearch 中 _primary_term 只需要通过 doc_id 读取到即可，所以只需要保存为 DocValues 就可以了。指的是文档所在位置。
         *
         */
        request.setIfSeqNo(100);
        request.setIfPrimaryTerm(2);

        // 同步方法，及处理文档版本冲突的异常
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {

            }
        } catch (IOException e) {
        } catch (ElasticsearchException exception) {
            if (exception.status() == RestStatus.CONFLICT) {

            }
        }

        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {

                // 获取索引名称，文档 id
                String index = deleteResponse.getIndex();
                String id = deleteResponse.getId();

                long version = deleteResponse.getVersion();
                ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

                // 处理成功分片数量少于总分片数量的情况
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

                }
                // 处理潜在的故障
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                        String reason = failure.reason();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        restHighLevelClient.deleteAsync(request, RequestOptions.DEFAULT, listener);
    }

    private boolean update(String indexId){

        UpdateRequest request = new UpdateRequest(indexName, indexId);

        // 使用脚本更新，具体用法可以参考官方文档。
        Map<String, Object> parameters = new HashMap<String, Object>(){{
            put("count", 4);
        }};
        // 创建一个内联脚本，参数是一个 map
        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.field += params.count", parameters);
        request.script(inline);

        // 使用部分文档进行更新，该部分内容会与已经存在的文档进行合并。且 doc 方法也可以接收一个 map 参数，它会自动转换为 json。
        String jsonString = "{" +
                "\"updated\":\"2017-01-01\"," +
                "\"reason\":\"test update\"" +
                "}";
        request.doc(jsonString, XContentType.JSON);


        // 如果该文档尚不存在，则可以使用 upsert 方法将参数作为新文档插入。与上述普通更新的方法相同，upsert 方法也可以使用
        // String，Map，XContentBuilder，Object 键对，或 IndexRequest作为入参，来定义 upsert 文档的内容。
        // 并且如果上述 doc 方法正常执行了（即对已有文档进行了更新），则此方法就不会被执行。反之，亦然。
        String jsonInsert = "{\"created\":\"2017-01-01\"}";
        request.upsert(jsonInsert, XContentType.JSON);


        // 同步方法
        try {
            UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();

        } catch (ElasticsearchException e) {
            if (e.status() == RestStatus.NOT_FOUND) {
            }
        }

        // 禁用空转
        request.detectNoop(false);
        // 启用源文档的搜索功能
        request.fetchSource(true);

        String[] includes = new String[]{"updated", "r*"};
        String[] excludes = Strings.EMPTY_ARRAY;
        request.fetchSource(new FetchSourceContext(true, includes, excludes));

        // 异步方法
        ActionListener<UpdateResponse> listener = new ActionListener<UpdateResponse>() {
            @Override
            public void onResponse(UpdateResponse updateResponse) {

                String index = updateResponse.getIndex();
                String id = updateResponse.getId();
                long version = updateResponse.getVersion();

                // 分别处理各种更新的响应状态，创建（upsert），更新，删除，空转（没有做任何操作）。
                if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {

                } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {

                } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {

                } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {

                }

                //当在更新请求中，开启了源文档搜索功能后。则响应将包含更新过的文档
                GetResult result = updateResponse.getGetResult();
                if (result.isExists()) {
                    String sourceAsString = result.sourceAsString();
                    Map<String, Object> sourceAsMap = result.sourceAsMap();
                    byte[] sourceAsBytes = result.source();
                } else {
                }

                ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
                if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                }
                if (shardInfo.getFailed() > 0) {
                    for (ReplicationResponse.ShardInfo.Failure failure :
                            shardInfo.getFailures()) {
                        String reason = failure.reason();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        restHighLevelClient.updateAsync(request, RequestOptions.DEFAULT, listener);
        return true;
    }

    private boolean multiGet(Map<String, String> searchMap){

        // 对多个索引进行文档搜索，indexname，docId，并禁止对源文档进行搜索（默认是开启的）。
        MultiGetRequest request = new MultiGetRequest();
        for(Map.Entry<String, String> entry : searchMap.entrySet()){
            request.add(
                    new MultiGetRequest.Item(entry.getKey(), entry.getValue())
                            .fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE)
            );
        }

        ActionListener<MultiGetResponse> listener = new ActionListener<MultiGetResponse>() {
            @Override
            public void onResponse(MultiGetResponse response) {
                /**
                 * 返回的 MultiGetResponse 包含一个 MultiGetItemResponse 中的列表。其顺序与 getRespons 的请求顺序相同。
                 * 如果成功获取，则 MultiGetItemResponse 包含 GetResponse。如果失败，则包含 MultiGetResponse.Failure。
                 */
                MultiGetItemResponse firstItem = response.getResponses()[0];

                // 当针对不存在的索引执行请求时，getFailure将包含异常：
                if(!ObjectUtils.isEmpty(firstItem.getFailure())){
                    Exception e = firstItem.getFailure().getFailure();
                    ElasticsearchException ee = (ElasticsearchException) e;
                    System.out.println(e.getMessage());
                }

                GetResponse firstGet = firstItem.getResponse();
                String index = firstItem.getIndex();
                String id = firstItem.getId();

                if (firstGet.isExists()) {
                    long version = firstGet.getVersion();
                    String sourceAsString = firstGet.getSourceAsString();
                    Map<String, Object> sourceAsMap = firstGet.getSourceAsMap();
                    byte[] sourceAsBytes = firstGet.getSourceAsBytes();
                } else {

                }
            }

            @Override
            public void onFailure(Exception e) {

            }
        };
        restHighLevelClient.mgetAsync(request, RequestOptions.DEFAULT, listener);
        return true;
    }
}
