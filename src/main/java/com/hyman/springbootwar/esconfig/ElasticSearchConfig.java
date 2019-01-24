package com.hyman.springbootwar.esconfig;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;


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
     * SpringBoot 2.X 的 spring-boot-starter-data-redis默认是以 lettuce作为连接池的， 而在 lettuce，elasticsearch transport
     * 中都会依赖netty，二者的netty 版本不一致，不能够兼容。所以 Springboot data 整合 Elasticsearch 时设置一下的属性，防止初始
     * 化 client时报错（不可以在主程序 main 方法中设置，没用）：
     * java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]
     *
     * 但是使用 jest 技术不报错，可以直接使用。
     */
    @PostConstruct
    void init() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    /**
     * 要特别注意，一定要安装当前 springboot data 指定的或以下的 ES 版本，否则不适配无法正常使用。在 parent 中查看集成的 ES 版本。
     * spring data elasticsearch	elasticsearch
         3.2.x	                    6.5.0
         3.1.x	                    6.2.2
         3.0.x	                    5.5.0
         2.1.x	                    2.4.0
         2.0.x	                    2.2.0
         1.3.x	                    1.5.2
     */
    @Bean
    public TransportClient transportClient() {

        /**
         * 注意：当 ES 服务器监听(publish_address )使用内网服务器IP，而访问(bound_addresses )使用外网 IP 时（yml 去掉注释后默认的
         * 配置），不要设置 client.transport.sniff为true，默认为false(关闭客户端去嗅探整个集群的状态)。因为在自动发现时会使用内网 IP
         * 进行通信，导致无法连接到 ES服务器。因此此时需要直接使用 addTransportAddress 方法把集群中其它机器的 ip 地址加到客户端中。
         *
         * 并且必须要修改默认的配置，开启 TransportClient 外网连接（0.0.0.0 不限制 IP），否则无法连接，找不到节点。
         */
        TransportClient client = null;
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticsearch")
                    // 不指定节点名称，自动适配
                    //.put("client.transport.ignore_cluster_name",true)
                    // sniff：嗅探
                    //.put("client.transport.sniff", true)
                    .build();

            //创建client
            client  = new PreBuiltTransportClient(settings)
                    //.addTransportAddress(new TransportAddress(InetAddress.getByName("IP"), 9300))
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.1.153"), 9300));

        } catch (Exception e) {
            client.close();
            e.printStackTrace();
        }
        return client;
    }
}