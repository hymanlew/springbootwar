package com.hyman.springbootwar.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 一、WebSocket 与 HTTP：
 * WebSocket 协议在2008年诞生，2011年成为国际标准。现在所有浏览器都已经支持了。
 * WebSocket 的最大特点就是，服务器可以主动向客户端推送信息，客户端也可以主动向服务器发送信息，是真正的双向平等对话。
 *
 * http 协议是应用层的协议，是基于 TCP 协议的，其建立链接必须要有三次握手才能发送信息。http链接分为短链接，长链接。短链接是每次请求都要三次握手才能发送自己的信息，即每一个request对应一个response。
 * 长链接是在一定的期限内保持链接。保持TCP连接不断开。客户端与服务器通信，必须要有客户端发起然后服务器返回结果。客户端是主动的，服务器是被动的。 
 * WebSocket 是HTML5中的协议， 是为了解决客户端发起多个http请求到服务器，而处理请求时必须经过长时间的轮训问题而生的。webSocket 实现了多路复用，是全双工通信。在webSocket协议下客服端和浏览器可
 * 以同时发送信息。
 *
 * 二、HTTP 的长连接与 websocket 的持久连接：
 * HTTP1.1 默认使用长连接（persistent connection），即在一定的期限内保持链接（keep-alive），把多个 HTTP 请求合并为一个，客户端才可以在短时间内向服务端请求大量的资源，保持TCP连接不断开。在一个TCP
 * 连接上可以传输多个Request/Response消息对，所以本质上还是Request/Response消息对，仍然会造成资源的浪费、实时性不强等问题。
 * 如果不是持续连接，即短连接，那么每个资源都要建立一个新的连接，HTTP底层使用的是TCP，那么每次都要使用三次握手建立TCP连接，即每一个request对应一个response，将造成极大的资源浪费。
 * 长轮询，即客户端发送一个超时时间很长的Request，服务器hold住这个连接，在有新数据到达时返回Response。
 *
 * Websocket 其实是一个新协议，但跟 HTTP 协议基本没有关系，只是为了兼容现有浏览器，所以在握手阶段使用了 HTTP。其持久连接只需建立一次Request/Response消息对，之后都是TCP连接，避免了需要多次建立
 * Request/Response消息对而产生的冗余头部信息。Websocket 只需要一次HTTP握手，所以说整个通讯过程是建立在一次连接/状态中，而且websocket可以实现服务端主动联系客户端，这是http做不到的。
 *
 * WebSocket 是 HTML5 开始提供的一种在单个 TCP 连接上进行全双工通讯的协议。它使得客户端和服务器之间的数据交换变得更加简单，允许服务端主动向客户端推送数据。在 WebSocket API中，浏览器和服务器只需
 * 要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。
 * 在 WebSocket API中，浏览器和服务器只需要做一个握手的动作，然后它们之间就形成了一条快速通道。两者之间就直接可以数据互相传送。
 *
 *
 * 开启WebSocket支持。
 * 通过实现 WebSocketConfigurer 类并覆盖相应的方法进行 websocket 的配置。
 * 通过这个配置 spring boot 才能去扫描后面的关于 websocket 的注解 WsServerEndpoint。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketHandler httpAuthHandler;
    @Autowired
    private MyInterceptor myInterceptor;

    /**
     * 重写该方法，通过向 WebSocketHandlerRegistry 设置不同参数来进行配置。其中：
     * addHandler       添加自定义的 handler 处理类，第二个参数是你暴露出的 ws 路径。
     * addInterceptors  添加自定义的握手拦截器。
     * setAllowedOrigins("*") 这个是关闭跨域校验（即允许跨域），方便本地调试，线上推荐打开。
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(httpAuthHandler, "/websocket")
                .addInterceptors(myInterceptor)
                .setAllowedOrigins("*");

        // WebSocket对象是H5新增的对象，低版本浏览器可能不支持，可以使用sockJs的方式连接
        // sockJs通道
        registry.addHandler(httpAuthHandler, "/sock-js")
                .addInterceptors(myInterceptor)
                .setAllowedOrigins("*")
                // 开启sockJs支持
                .withSockJS();
    }

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}
