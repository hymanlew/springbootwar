package com.hyman.springbootwar.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * 需要注意一下，此类使用的注解及参数（websocket，Session）都是在 javax.websocket 下，是 jdk 自带的，即它们都是 java 原生的
 * 注解与参数，并不是 spring 提供的。
 *
 * @ServerEndpoint
 * 通过这个 spring boot 就可以知道你暴露出去的访问应用的路径，类似 @RequestMapping。比如启动端口是 8080，而这个注解的值是 ws，
 * 则就可以通过 ws://127.0.0.1:8080/ws 来连接你的应用
 *
 * @OnOpen 当 websocket 建立连接成功后会触发这个注解修饰的方法，它有一个 Session 参数。
 * @OnClose 当 websocket 建立的连接断开后会触发这个注解修饰的方法，它有一个 Session 参数。
 * @OnMessage 当客户端发送消息到服务端时，会触发这个注解修改的方法，它有一个 String 入参表明客户端传入的值。
 * @OnError 当 websocket 建立连接时出现异常会触发这个注解修饰的方法，它有一个 Session 参数。
 *
 * 需要注意的是，服务端发送消息给客户端必须通过上面说的 Session 类（spring 提供了 WebSocketSession），通常是在 @OnOpen 方法中，
 * 当连接成功后把 session 存入 Map 的 value，key 是与 session 对应的用户标识，当要发送的时候通过 key 获得 session 再发送。
 * 可以通过 session.getBasicRemote().sendText(*) 来对客户端发送消息。
 */
@Slf4j
@ServerEndpoint("/myWs")
@Component
public class WsServerEndpoint {

    /**
     * 连接成功
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("连接成功");
    }

    /**
     * 连接关闭
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        log.info("连接关闭");
    }

    /**
     * 接收到消息
     *
     * @param text
     */
    @OnMessage
    public String onMsg(String text) throws IOException {
        return "servet 发送：" + text;
    }
}
