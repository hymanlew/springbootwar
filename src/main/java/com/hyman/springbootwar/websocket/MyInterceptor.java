package com.hyman.springbootwar.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过实现 HandshakeInterceptor 接口来定义握手拦截器，它与上面 HttpAuthHandler 的事件是不同的。这里是建立握手时的事件，分为
 * 握手前与握手后，而 HttpAuthHandler 是在握手成功后的基础上建立 socket 的连接。所以在如果把认证放在这个步骤相对来说最节省服务
 * 器资源。
 * 它主要有两个方法 beforeHandshake 与 afterHandshake，顾名思义一个在握手前触发，一个在握手后触发。
 * attributes 属性最终在 WebSocketSession 里，可通过 webSocketSession.getAttributes().get(key值) 获得。
 */
@Component
public class MyInterceptor implements HandshakeInterceptor {

    /**
     * 握手前
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            System.out.println("握手开始");

            // 获得请求参数
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            String token = serverHttpRequest.getServletRequest().getParameter("token");

            //HashMap<String, String> paramMap = HttpUtil.decodeParamMap(request.getURI().getQuery(), "utf-8");
            Map<String, String> paramMap = getStringToMap(request.getURI().getQuery());
            String uid = paramMap.get("token");
            if (uid != null && !"".equals(uid)) {
                // 放入属性域
                attributes.put("token", uid);
                System.out.println("用户 token " + uid + " 握手成功！");
                return true;
            }
            System.out.println("用户登录已失效");
            return false;
        } else {
            return false;
        }
    }

    /**
     * 握手后
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        System.out.println("握手完成");
    }

    public static Map<String, String> getStringToMap(String str) {
        //判断str是否有值
        if (null == str || "".equals(str)) {
            return null;
        }

        //根据&截取
        String[] strings = str.split("&");

        //设置HashMap长度
        int mapLength = strings.length;

        //判断hashMap的长度是否是2的幂。
        if ((strings.length % 2) != 0) {
            mapLength = mapLength + 1;
        }

        Map<String, String> map = new HashMap<>(mapLength);
        //循环加入map集合
        for (int i = 0; i < strings.length; i++) {
            //截取一组字符串
            String[] strArray = strings[i].split("=");
            //strArray[0]为KEY  strArray[1]为值
            map.put(strArray[0], strArray[1]);
        }
        return map;
    }
}
