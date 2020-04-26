package com.hyman.springbootwar.esconfig;

import com.google.gson.Gson;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class EsTest {

    @Resource
    private RestHighLevelClient restHighLevelClient;
    private String indexName = "indexname";

    @RequestMapping(value = "hello")
    public String hello() {

        Map<String, Object> query = new HashMap<>();

        try {
            Goods goods = new Goods(1L,"Gary1", "gggggggggggggggggggggggggg");
            // 注意indexId是唯一的
            String indexId = "test001";
            add(goods, indexId);

            goods = new Goods(2L,"Gary2", "kkkkkkkkkkkkkkkkkkkk");
            // 注意indexId是唯一的
            indexId = "test002";
            add(goods, indexId);

            query = this.query("test002");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Hello World!" + query.toString();
    }

    private boolean add(Goods goods, String indexId) throws IOException {
        String json = new Gson().toJson(goods);
        IndexRequest request = new IndexRequest(indexName).id(indexId).source(json, XContentType.JSON);
        restHighLevelClient.index(request, RequestOptions.DEFAULT);

        return true;
    }

    private Map<String, Object> query(String indexId) throws IOException {
        GetRequest request = new GetRequest(indexName, indexId);
        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);

        return response.getSource();
    }
}
