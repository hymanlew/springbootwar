package com.hyman.springbootwar.util;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Redis 的三个客户端实现：
 * jedis：是 Redis的 Java实现客户端，提供了比较全面的Redis命令的支持。使用阻塞的I/O，其方法调用都是同步的，程序流需要等到 sockets处理完
 *        I/O才能执行，不支持异步。当多线程使用同一个连接时，是线程不安全的。所以要使用连接池，为每个jedis实例分配一个连接。
 *
 * Lettuce：当多线程使用同一连接实例时，是线程安全的。基于Netty框架的事件驱动的通信层，其方法调用是异步的。是高级 Redis客户端，用于线程安
 *        全同步，异步和响应使用，支持集群，Sentinel，管道和编码器。主要在一些分布式缓存框架上使用比较多。
 *
 * Redisson：实现了分布式和可扩展的Java数据结构。基于Netty框架的事件驱动的通信层，其方法调用是异步的。Redisson的API是线程安全的，所以可以
 *        操作单个Redisson连接来完成各种操作。提供很多分布式相关操作服务，例如，分布式锁，分布式集合，可通过Redis支持延迟队列。
 *
 * 对于客户端的选择，尽量遵循各尽其用的原理，尽管 Jedis比起 Redisson有各种各样的不足，但也应该在需要使用 Redisson的高级特性时再选用 Redisson，
 * 避免造成不必要的程序复杂度提升。
 *
 * Jedis 是 Redis官方推荐的面向 Java的操作 Redis的客户端，而 RedisTemplate是 SpringDataRedis 中对 JedisApi 的高度封装。
 * springDataRedis 相对于 Jedis 来说可以方便地更换 Redis的 Java客户端，比 Jedis多了自动管理连接池的特性，方便与其他Spring框架进行搭配使用。如：SpringCache
 *
 *
 * Redis 测试：
 * 不可否认，Jedis 是一个优秀的基于 Java 语言的 Redis 客户端，但是其不足也很明显（详情在 RedisJedisConfig 类）：
 *
 * Jedis 在实现上是直接连接 Redis-Server，在多个线程间共享一个 Jedis 实例时是线程不安全的，如果想要在多线程场景下使用 Jedis，
 * 需要使用连接池，每个线程都使用自己的 Jedis 实例，当连接数量增多时，会消耗较多的物理资源。
 *
 * 与 Jedis 相比，Lettuce 则完全克服了其线程不安全的缺点：
 * Lettuce 是一个可伸缩的线程安全的 Redis 客户端，支持同步、异步和响应式模式。多个线程可以共享一个连接实例，而不必担心多线程并
 * 发问题。它基于优秀 Netty NIO 框架构建，支持 Redis 的高级功能，如 Sentinel，集群，流水线，自动重新连接和 Redis 数据模型。
 *
 *
 * Lettuce 重要接口介绍；
 * Redis单机模式下，Lettuce 的使用；
 * Redis集群模式下，Lettuce 的使用；
 * 使用 Lettuce 创建 Redis 集群；
 * 使用 Lettuce 监控 Redis；
 * Lettuce 使用过程中的“坑”：堆内存溢出和堆外内存溢出。
 *
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
        // 创建 RedisClient
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1");
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        connect.sync().set("key1", "value2");
        String value1 = connect.sync().get("key1");
        //测试
        if(!"value1".equals(value1)){

        }
        connect.close();
        redisClient.shutdown();
    }

    /**
     * 场景一： 位操作 签到
     */
    public void signIn() {
        // 创建 RedisClient
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1");
        //同步连接 (耗时 28秒)
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        for (int i = 0; i < 1000000; i++) {
            connect.sync().setbit("signIn_20190221", i, 1);
        }

        //关闭连接
        connect.close();
        //停止
        redisClient.shutdown();
    }

    /**
     * 场景一： 位操作 签到
     */
    public void signInAsync() {
        // 创建 RedisClient
        RedisClient redisClient = RedisClient.create("redis://127.0.0.1");
        // 异步连接，统计 (100万 耗时 4秒 内存空间占用 122.07)
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        for (int i = 0; i < 1000000; i++) {
            connect.async().setbit("signIn", i, 1);
        }
        //关闭连接
        connect.close();
        //停止
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
}
