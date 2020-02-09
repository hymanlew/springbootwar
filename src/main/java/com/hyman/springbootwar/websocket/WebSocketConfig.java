package com.hyman.springbootwar.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 通过实现 WebSocketConfigurer 类并覆盖相应的方法进行 websocket 的配置。
 * 通过这个配置 spring boot 才能去扫描后面的关于 websocket 的注解 WsServerEndpoint。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private HttpAuthHandler httpAuthHandler;
    @Autowired
    private MyInterceptor myInterceptor;

    /**
     * 重写该方法，通过向 WebSocketHandlerRegistry 设置不同参数来进行配置。其中：
     * addHandler 方法添加自定义的 handler 处理类，第二个参数是你暴露出的 ws 路径。
     * addInterceptors 添加自定义的握手过滤器。
     * setAllowedOrigins("*") 这个是关闭跨域校验，方便本地调试，线上推荐打开。
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(httpAuthHandler, "myWS")
                .addInterceptors(myInterceptor)
                .setAllowedOrigins("*");
    }

    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
}
