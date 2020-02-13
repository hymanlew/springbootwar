package com.hyman.springbootwar.websocket;

import java.io.Serializable;

public class Talker implements Serializable {

    // 用户登录唯一验证
    private String token;
    // websocketID
    private String sessionID;
    // 房间号
    private String roomID;
    // 直播号
    private String liveid;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getLiveid() {
        return liveid;
    }

    public void setLiveid(String liveid) {
        this.liveid = liveid;
    }

}
