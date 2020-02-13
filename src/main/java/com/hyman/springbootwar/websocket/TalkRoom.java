package com.hyman.springbootwar.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室
 */
public class TalkRoom implements Serializable {

    private ConcurrentHashMap<String, WebSocketSession> sessionMap;

    public TalkRoom() {
        sessionMap = new ConcurrentHashMap<String, WebSocketSession>(10000);
    }

    public TalkRoom(int size) {
        sessionMap = new ConcurrentHashMap<String, WebSocketSession>(size);
    }

    public ConcurrentHashMap<String, WebSocketSession> getSessionMap() {
        return sessionMap;
    }

    public void setSessionMap(
            ConcurrentHashMap<String, WebSocketSession> sessionMap) {
        this.sessionMap = sessionMap;
    }

    public void add(String sessionId, WebSocketSession session) {
        this.sessionMap.put(sessionId, session);
    }

    public WebSocketSession get(String sessionId) {
        return this.sessionMap.get(sessionId);
    }

    /**
     * 删除并同步关闭连接
     *
     * @param sessionId
     */
    public void removeAndClose(String sessionId) {
        WebSocketSession session = remove(sessionId);
        if (session != null) {
            try {
                // 关闭连接
                session.close();
            } catch (IOException e) {
                // 关闭出现异常处理
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除 session，会返回删除的 session
     *
     * @param sessionId
     * @return
     */
    private WebSocketSession remove(String sessionId) {
        if (null == sessionId) {
            return null;
        }
        return this.sessionMap.get(sessionId);
    }


    /**
     * 单条发送，实现服务器主动推送
     *
     * @param sessionId
     * @param message
     * @throws Exception
     */
    public void send(String liveid, String sessionId, String message) throws Exception {
        this.sessionMap.get(sessionId).sendMessage(new TextMessage("server 发送给 " + liveid + " 消息 " + message + " " + LocalDateTime.now().toString()));
    }

    /**
     * 群发，实现服务器主动推送
     *
     * @param message
     */
    public void batchSend(String message) throws Exception {
        for (String item : sessionMap.keySet()) {
            sessionMap.get(item).sendMessage(new TextMessage(message));
        }
    }

}
