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
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * 最初将索引比成数据库，将Type类型比成表。这是一个错误的类比，导致了错误的假设。在SQL数据库中，表是相互独立的。一个表中的列与
 * 另一个表中具有相同名称的列没有关系。但这与映射类型中的字段不同。在 Elasticsearch 索引中，不同映射类型中具有相同名称的字段在
 * 内部由相同的 Lucene 字段支持。例如 user 类型中的 name 字段存储在与 person类型中的 name字段完全相同的字段中，而且两个 name
 * 字段在两种类型中必须具有相同的映射（定义）。
 * 如此，当删除同一索引中的一个类型的日期字段和另一个类型的布尔字段时，这可能会导致问题。最重要的是，存储在同一索引中具有很少或
 * 没有字段的不同实体会导致数据稀疏，并影响 Lucene 有效压缩文档的能力。基于这些原因，ES7.0 决定将映射类型的概念从 Elasticsearch 中移除。
 *
 * 映射类型编辑的替代方案，按文档类型编辑索引：
 * 1，第一种方法是为每个文档类型建立一个索引。可以将 person存储在 person索引中，而将用户存储在用户索引中，而不是将两种文档存储
 * 在单个索引中。索引之间是完全独立的，因此在索引之间不存在字段类型的冲突。这种方 法有两个好处：
 * 数据更可能是密集的，因此受益于 Lucene 中使用的压缩技术。
 * 用于在全文搜索中评分的术语统计信息更有可能是准确的，因为同一索引中的所有文档都表示单个实体。
 * 每个索引都可以根据其包含的文档数量适当调整大小，调整主碎片的多少。
 *
 * 2，自定义类型 fieldedit：
 * 集群中可以存在多少主碎片是有限制的，因此不能为了收集几千个文档而浪费整个碎片。这时就可以实现自己的自定义类型字段，其工作方式
 * 与旧的 _type 类似。以用户 /tweet 为例。最初，工作流应该是这样的:
 PUT twitter
 {
 "mappings": {
 "user": {
 "properties": {
 "name": { "type": "text" },
 "user_name": { "type": "keyword" },
 "email": { "type": "keyword" }
 }
 },
 "tweet": {
 "properties": {
 "content": { "type": "text" },
 "user_name": { "type": "keyword" },
 "tweeted_at": { "type": "date" }
 }
 }
 }
 }

 PUT twitter/user/kimchy
 {
 "name": "Shay Banon",
 "user_name": "kimchy",
 "email": "shay@kimchy.com"
 }

 PUT twitter/tweet/1
 {
 "user_name": "kimchy",
 "tweeted_at": "2017-10-24T09:00:00Z",
 "content": "Types are going away"
 }

 GET twitter/tweet/_search
 {
 "query": {
 "match": {
 "user_name": "kimchy"
 }
 }
 }

 现在，可以通过添加自定义类型字段来实现相同的功能，如下所示:
 PUT twitter
 {
 "mappings": {
 "_doc": {
 "properties": {
 "type": { "type": "keyword" },
 "name": { "type": "text" },
 "user_name": { "type": "keyword" },
 "email": { "type": "keyword" },
 "content": { "type": "text" },
 "tweeted_at": { "type": "date" }
 }
 }
 }
 }

 PUT twitter/_doc/user-kimchy
 {
 "type": "user",
 "name": "Shay Banon",
 "user_name": "kimchy",
 "email": "shay@kimchy.com"
 }

 PUT twitter/_doc/tweet-1
 {
 "type": "tweet",
 "user_name": "kimchy",
 "tweeted_at": "2017-10-24T09:00:00Z",
 "content": "Types are going away"
 }

 GET twitter/_search
 {
 "query": {
 "bool": {
 "must": {
 "match": {
 "user_name": "kimchy"
 }
 },
 "filter": {
 "match": {
 "type": "tweet"
 }
 }
 }
 }
 }

 * 以前，父子关系通过将一个映射类型表示为父类型，将一个或多个其他映射类型表示为子类型。没有类型，我们就不能再使用这种语法。除了
 * 示文档之间关系的方式已更改为使用新的连接字段外，父子功能将继续像以前一样工作。
 * ES7.0 不赞成在请求中指定类型。且索引文档不再需要文档类型。对于显式id，新的索引 api 是 PUT {index}/_doc/{id}。对于自动生成
 * 的id，则是 POST {index}/_doc。注意，在7.0中 _doc是路径的永久部分，它表示端点名称，而不是文档类型。
 * 索引创建、索引模板和映射 api 中的 include_type_name 参数默认为 false。在所有的设置参数将提示一个弃用警告。并且删除了 _default_
 * 映射类型。
 *
 * 将多类型索引迁移到单类型编辑：Reindex API 可用于将多类型索引转换为单类型索引。
 */
@Component
public class ElasticServiceUtils {

    /**
     * hosts :配置的值
     */
    @Value("${elasticsearch.hosts}")
    private String hostAddress;

    /**
     * 确保地址为ip+端口形式
     */
    private final static Integer ADDRESS_LENGTH = 2;

    private static final String HTTP_SCHEME = "http";

    /**
     * logger :SLF4J日志
     */
    private final static Logger logger = LoggerFactory.getLogger(ElasticServiceUtils.class);

    /**
     * Java 高级别REST客户端（The Java High Level REST Client），内部仍然是基于低级客户端。它提供了更多的API，接受请求对象作
     * 为参数并返回响应对象，由客户端自己处理编码和解码。
     * 每个API都可以同步或异步调用。 同步方法返回一个响应对象，而异步方法的名称以async后缀结尾，需要一个监听器参数，一旦收到响
     * 应或错误，就会被通知（由低级客户端管理的线程池）。
     * 高级客户端依赖于Elasticsearch core项目。 它接受与TransportClient相同的请求参数并返回相同的响应对象。
     */
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

            if (StringUtils.isEmpty(hostAddress)) {
                logger.error("ipAddress is null");
                throw new RuntimeException("ipAddress is null");
            }

            // 根据多地址组装hosts，同时创建多个节点
            HttpHost[] hosts = Arrays.stream(hostAddress.split(","))
                    .map(this::makeHttpHost)
                    .filter(Objects::nonNull)
                    .toArray(HttpHost[]::new);

            //HttpHost node1 = new HttpHost(host, port, "http");

            // builder 方法有两个入参。第一个是传入 Node(可以包含多个节点，也可以设置密码)。第二个就是传入 HttpHost。
            //RestClientBuilder builder = RestClient.builder(node1);

            RestClientBuilder builder = RestClient.builder(hosts);
            restHighLevelClient = new RestHighLevelClient(builder);

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return restHighLevelClient;
    }

    /**
     * 组装 HttpHost 对象
     */
    private HttpHost makeHttpHost(String ipAddress) {
        assert !StringUtils.isEmpty(ipAddress);

        String[] address = ipAddress.split(":");
        if (address.length == ADDRESS_LENGTH) {
            String ip = address[0];
            int port = Integer.parseInt(address[1]);
            return new HttpHost(ip, port, HTTP_SCHEME);
        } else {
            return null;
        }
    }
}
