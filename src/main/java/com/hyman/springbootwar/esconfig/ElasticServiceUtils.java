package com.hyman.springbootwar.esconfig;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 当直接在ElasticSearch 建立文档对象时，如果索引不存在的，默认会自动创建，映射采用默认方式。
 *
 */
@Component
public class ElasticServiceUtils {

    /**
     * hosts :配置的值
     */
    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private Integer port;

    /**
     * logger :SLF4J日志
     */
    private final static Logger logger = LoggerFactory.getLogger(ElasticServiceUtils.class);

    private RestHighLevelClient restHighLevelClient;

    /**
     * 在 Servlet 容器初始化前执行
     */
    @PostConstruct
    private void init() {

        // 也可以在此方法中，配置 ES 的连接及其他设置。
    }

    /**
     * 这里只是配置了对 ES 节点的连接，可以对其进行操作。而 ES 集群的搭建及其配置，是在 es 的配置文件中声明的。
     * @return
     */
    @Bean(destroyMethod = "close")
    public RestHighLevelClient client() {

        try {
            // 关闭客户端连接
            if (restHighLevelClient != null) {
                restHighLevelClient.close();
            }

            // 可以同时创建多个节点
            HttpHost node1 = new HttpHost(host, port, "http");

            // builder 方法有两个入参。第一个是传入 Node(可以包含多个节点，也可以设置密码)。第二个就是传入 HttpHost。
            RestClientBuilder builder = RestClient.builder(node1);
            restHighLevelClient = new RestHighLevelClient(builder);

            //restHighLevelClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(hosts[0]), HttpHost.create(hosts[1])));

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return restHighLevelClient;
    }


    //省略创建索引更新索引等代码,官网有具体的例子.
    //官网地址: https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html


    public void v1(){

    }
}
