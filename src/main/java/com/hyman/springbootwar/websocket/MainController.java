package com.hyman.springbootwar.websocket;

import com.hyman.springbootwar.util.CacheProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * websocket 存储 session（适合聊天，转发，广播，服务器主动发送消息）。即只是将 websocket 中的 session 提取并存储起来，便于
 * 用户之间的互动与查找，其实就是解耦合。
 *
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
    @RequestMapping("/socket/push/{liveId}")
    public Map pushToWeb(@PathVariable String liveId, String message) {
        Map result = new HashMap();
        try {
            CacheProvider.get(liveId);
            result.put("code", 200);
            result.put("msg", "success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
