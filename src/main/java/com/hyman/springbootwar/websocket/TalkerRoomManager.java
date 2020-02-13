package com.hyman.springbootwar.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.thymeleaf.util.StringUtils;

import javax.websocket.Session;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 该类也可以充当聊天室管理器
 */
@Component
@Slf4j
public class TalkerRoomManager {

    // concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    //private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

    /**
     * 通过线程安全的 ConcurrentHashMap 实现了一个 session 池，用来保存已经登录的 websocket的session。
     * 前文提过，服务端发送消息给客户端必须要通过这个 session（java 原生提供的 javax.websocket.Session）。
     * <p>
     * 可以充当房间池。
     */
    private static ConcurrentHashMap<String, TalkRoom> ROOM_POOL = new ConcurrentHashMap<>();

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);


    /**
     * 删除 session，会返回删除的 session
     *
     * @param talker
     * @return
     */
    public static void remove(Talker talker) {
        try {
            if (null == talker) {
                return;
            }
            TalkRoom room = ROOM_POOL.get(talker.getRoomID());
            if (null == room) {
                return;
            }

            room.removeAndClose(talker.getSessionID());
            // 在线数减1
            subOnlineCount();
            log.info("有一连接关闭！当前在线人数为" + getOnlineCount());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获得用户 session
     *
     * @param talker
     * @return
     */
    public static WebSocketSession getSession(Talker talker) {
        try {
            if (null == talker) {
                return null;
            }
            TalkRoom room = ROOM_POOL.get(talker.getRoomID());
            if (null == room) {
                return null;
            }
            WebSocketSession session = room.get(talker.getSessionID());
            return session;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取当前房间实体
     *
     * @param roomID
     * @return
     */
    public static TalkRoom getRoom(String roomID) {
        try {
            if (StringUtils.isEmpty(roomID)) {
                return null;
            }
            TalkRoom room = ROOM_POOL.get(roomID);
            return room;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 添加用户进入房间并是否创建房间
     */
    public static synchronized void creatRoom(WebSocketSession session, Talker talker) {
        TalkRoom room = ROOM_POOL.get(talker.getRoomID());
        if (null == room) {
            room = new TalkRoom();
            ROOM_POOL.put(talker.getRoomID(), room);
        }
        room.add(talker.getSessionID(), session);

        // 在线数加 1
        addOnlineCount();
        log.info("有新窗口开始监听:" + talker.getRoomID() + ",当前在线人数为" + getOnlineCount());

        try {
            send(talker, "连接成功");
        } catch (Exception e) {
            log.error("websocket IO异常");
        }
    }

    /**
     * 删除房间
     */
    public static synchronized void deleteRoom(String roomID) {
        TalkRoom socketRoom = ROOM_POOL.get(roomID);
        if (null == socketRoom) {
            return;
        }
        if (socketRoom.getSessionMap().size() == 0) {
            ROOM_POOL.remove(roomID);
        }
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
     * 单条发送，实现服务器主动推送。
     *
     * 这里主要是将处理消息和session分离开，唯一要注意的是移动端的 websocket 在退出时服务端可能接收不到，一定要注意将 session
     * 清空。否则 session 会越寸越大导致内存溢出。
     *
     * @param talker
     * @param message
     * @throws Exception
     */
    public static void send(Talker talker, String message) throws Exception {
        ROOM_POOL.get(talker.getRoomID()).send(talker.getLiveid(), talker.getSessionID(), message);
    }

    /**
     * 群发，实现服务器主动推送
     *
     * @param message
     */
    public static void batchSend(String message) {
        try {
            for (String item : ROOM_POOL.keySet()) {
                ROOM_POOL.get(item).batchSend(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
