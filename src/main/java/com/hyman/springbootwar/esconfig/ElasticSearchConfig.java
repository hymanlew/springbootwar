package com.hyman.springbootwar.esconfig;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
public class ElasticSearchConfig {
    /**
     * springboot 默认支持两种技术和 ES（ElasticSearch）交互：
     * 1，jest（但不默认生效，是不使用的，需要导入 io.searchbox.client.JestClient，配置 spring.elasticsearch.jest）
     *
     * 2，springdata ElasticSearch（默认使用，需要配置 spring.data.elasticsearch，）
     * 	 1，Client 节点信息，clusterNodes，clusterName
     * 	 2，ElasticsearchTemplate 操作 ES
     * 	 3，编写 repository 接口
     */
    /**
     * SpringBoot 2.X 的 spring-boot-starter-data-redis 默认是以 lettuce 作为连接池的， 而在 lettuce ， elasticsearch transport 中都会依赖netty, 二者的netty 版本不一致，不能够兼容
     * Springboot data 整合 Elasticsearch 在项目启动前设置一下的属性，防止初始化 client时报错（不可以在主程序 main 方法中设置，没用）：
     * java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]
     *
     * 但是使用 jest 技术不报错，可以直接使用。
     */
    @PostConstruct
    void init() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    /**
     * 版本不适配，查看下表，再查看 boot 集成的版本，然后安装对应的版本。暂不可用，有异常
     * spring data elasticsearch	elasticsearch
         3.2.x	                    6.5.0
         3.1.x	                    6.2.2
         3.0.x	                    5.5.0
         2.1.x	                    2.4.0
         2.0.x	                    2.2.0
         1.3.x	                    1.5.2
     */
    //@Bean
    //@Bean(name = "transportClient")
    //public TransportClient transportClient() {
    //
    //    /**
    //     * sniff：嗅探
    //     */
    //    TransportClient client = null;
    //    try {
    //        Settings settings = Settings.builder()
    //                .put("cluster.name", "TPfVC5_")
    //                //.build();
    //                //.put("client.transport.ignore_cluster_name",true)
    //                .put("client.transport.sniff", true)
    //                //.put("sniffOnConnectionFault",true)
    //                .build();
    //        //client = new PreBuiltTransportClient(settings)
    //
    //        //client = new PreBuiltTransportClient(Settings.EMPTY)
    //                //.addTransportAddress(new TransportAddress(InetAddress.getByName("host1"), 9300))
    //                //.addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.1.153"), 9301));
    //
    //        //创建client
    //        client  = new PreBuiltTransportClient(settings)
    //                //.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.1.153"), 9300));
    //                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.1.153"), 9300));
    //
    //
    //    } catch (UnknownHostException e) {
    //        client.close();
    //        e.printStackTrace();
    //    }
    //    return client;
    //
    //}

    //@Bean
    //public ElasticsearchOperations elasticsearchTemplateCustom() {
    //    Client client = client();
    //    if (client != null) {
    //        return new ElasticsearchTemplate(client);
    //    } else {
    //        //弹出自定义异常对象
    //        throw new RuntimeException("初始化Elasticsearch失败！ 100011");
    //    }
    //}

    //Embedded Elasticsearch Server
    /*@Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
    }*/
}