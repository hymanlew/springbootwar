package com.hyman.springbootwar.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WsSessionManager {

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    //private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    /**
     * 通过线程安全的 ConcurrentHashMap 实现了一个 session 池，用来保存已经登录的 websocket的session。
     * 前文提过，服务端发送消息给客户端必须要通过这个 session（java 原生提供的 javax.websocket.Session）。
     */
    private static ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);


    /**
     * 添加 session
     *
     * @param key
     */
    public static void add(String key, WebSocketSession session) {
        SESSION_POOL.put(key, session);

        // 在线数加 1
        addOnlineCount();
        log.info("有新窗口开始监听:" + key + ",当前在线人数为" + getOnlineCount());

        try {
            send(key,"连接成功");
        } catch (Exception e) {
            log.error("websocket IO异常");
        }
    }

    /**
     * 删除 session，会返回删除的 session
     *
     * @param key
     * @return
     */
    public static WebSocketSession remove(String key) {
        WebSocketSession socketSession = SESSION_POOL.remove(key);
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
        return socketSession;
    }

    /**
     * 删除并同步关闭连接
     *
     * @param key
     */
    public static void removeAndClose(String key) {
        WebSocketSession session = remove(key);
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
     * 获得 session
     *
     * @param key
     * @return
     */
    public static WebSocketSession get(String key) {
        return SESSION_POOL.get(key);
    }

    public static int getOnlineCount() {
        return onlineCount.get();
    }
    public static void addOnlineCount() {
        onlineCount.getAndIncrement();
    }
    public static synchronized void subOnlineCount() {
        onlineCount.decrementAndGet();
    }

    /**
     * 单条发送，实现服务器主动推送
     *
     * @param key
     * @param message
     * @throws Exception
     */
    public static void send(String key, String message) throws Exception {
        SESSION_POOL.get(key).sendMessage(new TextMessage("server 发送给 " + key + " 消息 " + message + " " + LocalDateTime.now().toString()));
    }

    /**
     * 群发，实现服务器主动推送
     *
     * @param message
     */
    public static void batchSend(String message) {
        for (String item : SESSION_POOL.keySet()) {
            try {
                SESSION_POOL.get(item).sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
