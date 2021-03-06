package com.hyman.springbootwar.websocket;

import com.hyman.springbootwar.util.CacheProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.thymeleaf.util.StringUtils;

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
     * session 是与某个客户端的连接会话，需要通过它来给客户端发送数据。
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
     * 在 WebSocket 协商成功并且 WebSocket 连接打开并准备好使用后调用。
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Object token = session.getAttributes().get("sid");
        String sid = session.getUri().getQuery();

        try {
            if (token != null) {
                Talker talker = getTalker(session);

                // 用户连接成功，放入本地在线用户缓存
                // 用户连接成功，放入 redis
                //CacheProvider.set(session.getId(), session);

                //创建房间并加入session
                TalkerRoomManager.creatRoom(session, talker);
            } else {
                throw new RuntimeException("用户登录已经失效!");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 接收消息事件，在新的WebSocket消息到达时调用,也就是接受客户端信息并发发送
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        Talker talker = getTalker(session);
        log.info("收到来自窗口" + talker.getLiveid() + "的信息:" + message);

        // 获得客户端传来的消息
        String payload = message.getPayload();
        // 空消息不做操作
        if (StringUtils.isEmpty(payload)) {
            return;
        }
        System.out.println("server 接收到 " + talker.getLiveid() + " 发送的 " + payload);

        // 后端发送自己的消息
        TalkerRoomManager.send(talker, payload);

        // 后端群发消息
        TalkerRoomManager.batchSend(payload);
    }

    /**
     * 处理底层WebSocket消息传输中的错误，连接出现异常时触发
     * @param session
     * @param exception
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        afterConnectionClosed(session, null);
        super.handleTransportError(session, exception);
    }

    /**
     * socket 断开连接时，连接关闭调用的方法
     * 在任何一方关闭WebSocket连接之后或在发生传输错误之后调用。
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Object token = session.getAttributes().get("token");
        if (token != null) {
            Talker talker = getTalker(session);

            // 用户退出，移除缓存
            CacheProvider.del(session.getId());
            // 退出房间
            TalkerRoomManager.remove(talker);
            //是否删除房间
            TalkerRoomManager.deleteRoom(talker.getRoomID());
        }
    }

    private Talker getTalker(WebSocketSession session) {
        //获取用户信息
        String talkerString = CacheProvider.get(session.getId());
        Talker talker = CacheProvider.get(talkerString, Talker.class);
        if(null == talker){
            throw new RuntimeException("用户登录已经失效!");
        }
        return talker;
    }

    /**
     * WebSocketHandler是否处理部分消息，API文档描述说是拆分消息，多次处理，没有实际使用过
     * @return
     */
    @Override
    public boolean supportsPartialMessages() {
        return super.supportsPartialMessages();
    }
}
