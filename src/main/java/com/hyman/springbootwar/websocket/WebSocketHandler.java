package com.hyman.springbootwar.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.server.ServerEndpoint;

/**
 * 通过继承 TextWebSocketHandler 类并覆盖相应方法，可以对 websocket 的事件进行处理，这里可以与原生的那几个注解连起来看：
 * afterConnectionEstablished   方法是在 socket 连接成功后被触发，同原生注解里的 @OnOpen 功能。
 * afterConnectionClosed        方法是在 socket 连接关闭后被触发，同原生注解里的 @OnClose 功能。
 * handleTextMessage            方法是在客户端发送信息时触发，同原生注解里的  @OnMessage 功能。
 * handleTransportError         方法是在出现异常时触发，同原生注解里的  @OnError 功能。
 */
@ServerEndpoint("/websocket/{sid}")
@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private WebSocketSession session;


    /**
     *                实现服务器主动推送
     *                <p>
     *                群发自定义消息
     *                <p>
     *                连接关闭调用的方法
     *                <p>
     *                收到客户端消息后调用的方法
     * @param message 客户端发送过来的消息
     * @param session
     * @param error   <p>
     *                实现服务器主动推送
     *                <p>
     *                群发自定义消息
     */

    /**
     * socket 建立成功事件后调用的方法，收到客户端消息后调用的方法
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object token = session.getAttributes().get("sid");
        String sid = session.getUri().getQuery();

        this.session = session;
        if (token != null) {
            // 用户连接成功，放入在线用户缓存
            WsSessionManager.add(session.getId(), session);
        } else {
            throw new RuntimeException("用户登录已经失效!");
        }
    }

    /**
     * 接收消息事件
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("收到来自窗口" + session.getId() + "的信息:" + message);

        // 获得客户端传来的消息
        String payload = message.getPayload();
        Object sid = session.getAttributes().get("sid");
        System.out.println("server 接收到 " + sid + " 发送的 " + payload);

        // 后端发送自己的消息
        WsSessionManager.send(sid.toString(), payload);

        // 后端群发消息
        WsSessionManager.batchSend(payload);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        WsSessionManager.remove(session.getId());
        super.handleTransportError(session, exception);
    }

    /**
     * socket 断开连接时，连接关闭调用的方法
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object token = session.getAttributes().get("token");
        if (token != null) {
            // 用户退出，移除缓存
            WsSessionManager.remove(token.toString());
        }
    }
}
