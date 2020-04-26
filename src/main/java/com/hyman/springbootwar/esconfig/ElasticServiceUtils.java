package com.hyman.springbootwar.esconfig;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 第一种方式集成 ES：
 */
@Component
public class ElasticServiceUtils {

    /**
     * <li>logger :SLF4J日志 </li>
     */
    private final static Logger logger = LoggerFactory.getLogger(ElasticServiceUtils.class);


    private RestHighLevelClient restHighLevelClient;

    /**
     * <li>Description: 在Servlet容器初始化前执行 </li>
     */
    @PostConstruct
    private void init() {
        try {
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }
            //节点1和2
            HttpHost node1 = new HttpHost("192.168.10.40", 9200, "http");
            HttpHost node2 = new HttpHost("192.168.10.95", 9200, "http");
            RestClientBuilder builder = RestClient.builder(node1,node2);
            restHighLevelClient = new RestHighLevelClient(builder);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }
    //省略创建索引更新索引等代码,官网有具体的例子.
    //官网地址: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html
}
