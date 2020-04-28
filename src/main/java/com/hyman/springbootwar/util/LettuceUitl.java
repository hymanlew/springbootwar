package com.hyman.springbootwar.util;

import com.alibaba.druid.support.json.JSONUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Redis 的三个客户端实现：
 * jedis：是 Redis的 Java实现客户端，提供了比较全面的Redis命令的支持。使用阻塞的I/O，其方法调用都是同步的，程序流需要等到 sockets处理完
 * I/O才能执行，不支持异步。当多线程使用同一个连接时，是线程不安全的。所以要使用连接池，为每个jedis实例分配一个连接。
 * <p>
 * Lettuce：当多线程使用同一连接实例时，是线程安全的。基于Netty框架的事件驱动的通信层，其方法调用是异步的。是高级 Redis客户端，用于线程安
 * 全同步，异步和响应使用，支持集群，Sentinel，管道和编码器。主要在一些分布式缓存框架上使用比较多。
 * <p>
 * Redisson：实现了分布式和可扩展的Java数据结构。基于Netty框架的事件驱动的通信层，其方法调用是异步的。Redisson的API是线程安全的，所以可以
 * 操作单个Redisson连接来完成各种操作。提供很多分布式相关操作服务，例如，分布式锁，分布式集合，可通过Redis支持延迟队列。
 * <p>
 * 对于客户端的选择，尽量遵循各尽其用的原理，尽管 Jedis比起 Redisson有各种各样的不足，但也应该在需要使用 Redisson的高级特性时再选用 Redisson，
 * 避免造成不必要的程序复杂度提升。
 * <p>
 * Jedis 是 Redis官方推荐的面向 Java的操作 Redis的客户端，而 RedisTemplate是 SpringDataRedis 中对 JedisApi 的高度封装。
 * springDataRedis 相对于 Jedis 来说可以方便地更换 Redis的 Java客户端，比 Jedis多了自动管理连接池的特性，方便与其他Spring框架进行搭配使用。如：SpringCache
 * <p>
 * <p>
 * Redis 测试：
 * 不可否认，Jedis 是一个优秀的基于 Java 语言的 Redis 客户端，但是其不足也很明显（详情在 RedisJedisConfig 类）：
 * <p>
 * Jedis 在实现上是直接连接 Redis-Server，在多个线程间共享一个 Jedis 实例时是线程不安全的，如果想要在多线程场景下使用 Jedis，
 * 需要使用连接池，每个线程都使用自己的 Jedis 实例，当连接数量增多时，会消耗较多的物理资源。
 * <p>
 * 与 Jedis 相比，Lettuce 则完全克服了其线程不安全的缺点：
 * Lettuce 是一个可伸缩的线程安全的 Redis 客户端，支持同步、异步和响应式模式。多个线程可以共享一个连接实例，而不必担心多线程并
 * 发问题。它基于优秀 Netty NIO 框架构建，支持 Redis 的高级功能，如 Sentinel，集群，流水线，自动重新连接和 Redis 数据模型。
 * <p>
 * <p>
 * Lettuce 重要接口介绍；
 * Redis单机模式下，Lettuce 的使用；
 * Redis集群模式下，Lettuce 的使用；
 * 使用 Lettuce 创建 Redis 集群；
 * 使用 Lettuce 监控 Redis；
 * Lettuce 使用过程中的“坑”：堆内存溢出和堆外内存溢出。
 */
@Slf4j
@Configuration
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring.redis")
public class LettuceUitl {

    // Redis服务器地址
    @Value("${spring.redis.host}")
    private String host;
    // Redis服务器连接端口
    @Value("${spring.redis.port}")
    private Integer port;
    // Redis数据库索引（默认为0）
    @Value("${spring.redis.database}")
    private Integer database;
    // Redis服务器连接密码（默认为空）
    @Value("${spring.redis.password}")
    private String password;
    // 连接超时时间（毫秒）
    @Value("${spring.redis.timeout}")
    private Integer timeout;

    // 连接池最大连接数（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-active}")
    private Integer maxTotal;
    // 连接池最大阻塞等待时间（使用负值表示没有限制）
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private Integer maxWait;
    // 连接池中的最大空闲连接
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private Integer maxIdle;
    // 连接池中的最小空闲连接
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private Integer minIdle;
    // 关闭超时时间
    @Value("${spring.redis.lettuce.shutdown-timeout}")
    private Integer shutdown;


    public void simple() {

        // 创建 RedisClient，默认超时时间为 60 秒，端口为 6379
        //RedisClient redisClient = RedisClient.create("redis://127.0.0.1");
        //redisClient.setDefaultTimeout(Duration.ofSeconds(60));

        RedisURI redisUri = RedisURI.Builder.redis("localhost")
                .withPort(6379)
                .withTimeout(Duration.ofSeconds(60))
                .withSsl(true)
                .withPassword("123456")
                .withDatabase(0)
                .build();
        RedisClient redisClient = RedisClient.create(redisUri);
        StatefulRedisConnection<String, String> connect = redisClient.connect();

        // 同步连接
        connect.sync().set("key1", "value2");
        String value1 = connect.sync().get("key1");

        //同步连接 (耗时 28秒)
        for (int i = 0; i < 1000000; i++) {
            connect.sync().setbit("signIn_20190221", i, 1);
        }

        // 异步连接，统计 (100万 耗时 4秒 内存空间占用 122.07)
        for (int i = 0; i < 1000000; i++) {
            connect.async().setbit("signIn", i, 1);
        }

        try {
            /**
             * RedisFuture<T> 和 CompleteableFuture<T> 简介：
             * 每一个异步的 API 命令的调用都会创建一个可以取消、等待和订阅的 RedisFuture<T>。而一个 RedisFuture<T> 或 CompleteableFuture<T>
             * 是一个指向值计算尚未完成的最初未知结果的指针。
             * 一个 RedisFuture<T> 提供异步和链接的操作。异步通过 RedisFuture<T> 进行操作，同步直接通过同步执行命令进行操作。
             *
             * RedisFuture的get方法是阻塞方法，会一直阻塞到返回结果，可以添加超时时间。
             */
            RedisAsyncCommands<String, String> redisAsync = connect.async();

            /**
             * （拉模式）
             */
            // 没有设置超时
            RedisFuture<String> redisFuture = redisAsync.get("a");
            String a = redisFuture.get();
            System.out.println(a);

            // 设置超时
            RedisFuture<String> set = redisAsync.set("key", "value");
            RedisFuture<String> get = redisAsync.get("key");

            if (set.await(1, TimeUnit.MINUTES)) {
                if (!"OK".equals(set.get())) {
                    throw new RuntimeException("set await fail");
                }
            }
            // 阻塞同步
            if ("value".equals(get.get(1, TimeUnit.MINUTES))) {
                log.info("get success!");
            }

            /**
             * （推模式）
             */
            // 非阻塞，设置监听器
            get.thenAccept(new Consumer<String>() {
                //该方法将会在 get.complete() 方法执行后，自动执行。这样的执行流程就是推模式
                @Override
                public void accept(String value) {
                    log.info("get success!");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        connect.close();


        // lettuce 连接池的使用
        GenericObjectPool<StatefulRedisConnection<String, String>> pool = ConnectionPoolSupport
                .createGenericObjectPool(() -> redisClient.connect(), new GenericObjectPoolConfig());

        // executing work，基本使用
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> commands = connection.sync();
            // 启用事务
            commands.multi();
            commands.set("key", "value");
            commands.set("key2", "value2");
            commands.exec();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // execute work，集群使用
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            connection.sync().set("key", "value");
            connection.sync().blpop(10, "list");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // terminating
        pool.close();


        try {
            // 同步使用事务
            RedisCommands<String, String> redis = connect.sync();
            // 成功，则返回值为"OK"
            redis.multi();
            // 未执行，返回为null
            redis.set("", "");
            // 执行事务，返回list("OK")
            redis.exec();


            // 异步使用事务
            RedisAsyncCommands<String, String> async = connect.async();
            // 获取发送开启事务的future
            RedisFuture<String> multi = async.multi();
            RedisFuture<String> set = async.set("key", "value");

            // 获取提交执行事务的 future，及事务的结果
            RedisFuture<TransactionResult> exec = async.exec();
            List<Object> objects = (List<Object>) exec.get();

            // 测试事务操作与set操作结果是否一致，即事务操作是否成功
            String setResult = set.get();
            if (objects.get(0) == setResult) {
                log.info("set success!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        redisClient.shutdown();
    }


    /**
     * 使用批处理之后，加速，8s 表数据量 32M 按天
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void signInMySql() throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/redis?rewriteBatchedStatements=true";
        // 加载驱动
        Class.forName("com.mysql.jdbc.Driver");
        // 打开一个数据库连接
        Connection mysqlConnection = DriverManager.getConnection(url, "root", "123456");

        PreparedStatement ps = mysqlConnection.prepareStatement("insert into new_table values(?,?)");
        for (int i = 0; i < 1000000; i++) {
            ps.setInt(1, i);
            ps.setInt(2, i);
            ps.addBatch();

            if (i > 0 && i % 500 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
        ps.close();// 关闭Statement
        mysqlConnection.close(); // 关闭数据库连接
    }


    private static void demo() throws Exception {
        RedisURI redisURI = RedisURI.Builder.redis("localhost", 9379)
                .withDatabase(0)
                .withTimeout(Duration.ofSeconds(60))
                .withPassword("123456")
                .build();
        RedisClient redisClient = RedisClient.create(redisURI);
        RedisAsyncCommands<String, String> redis = redisClient.connect().async();

        // 返回名称为key的list中start至end之间的元素（下标从0开始，下同）
        RedisFuture<List<String>> futureList = redis.lrange("abc", 0, 10);
        if (null == futureList || futureList.get().isEmpty()) {
            return;
        }

        // 在名称为key的list头部添加一个值为value的 元素
        redis.lpush("abc", "test");
        // 返回并删除名称为key的list中的尾元素
        redis.rpop("abc");

        redis.setnx("abc", "test");
        redis.del("abc");
    }

}
