package com.hyman.springbootwar.websocket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 浏览器debug访问 localhost:8885/main/page/1 跳转到 websocket.html，页面中的 js 会自动连接server并传递 sid 到服务端，服务
 * 端对应的推送消息到客户端页面（sid 区分不同的请求，server里提供的有群发消息方法）
 */
@Controller
@RequestMapping("/main")
public class MainController {

    //页面请求
    @GetMapping("/page/{id}")
    public ModelAndView socket(@PathVariable String id) {
        ModelAndView mav=new ModelAndView("/websocket");
        mav.addObject("user", id);
        return mav;
    }

    //推送数据接口
    @ResponseBody
    @RequestMapping("/socket/push/{sid}")
    public Map pushToWeb(@PathVariable String cid, String message) {
        Map result = new HashMap();
        try {
            WsSessionManager.send(cid, message);
            result.put("code", 200);
            result.put("msg", "success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
