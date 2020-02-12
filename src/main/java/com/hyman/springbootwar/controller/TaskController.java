package com.hyman.springbootwar.controller;

import com.hyman.springbootwar.service.TaskService;
import com.hyman.springbootwar.util.CacheProvider;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.annotation.Resource;

@RestController
public class TaskController {

    @Resource
    private TaskService taskService;

    @RequestMapping("/NoAsync")
    public String NoAsync(){
        taskService.NoAsync("测试");
        return "同步";
    }

    @RequestMapping("/Async")
    public String Async(){
        taskService.Async("异步");
        return "异步";
    }


    @ResponseBody
    @RequestMapping("/test/lettuce")
    public String index(){

        StringBuilder str = new StringBuilder();

        str.append(CacheProvider.set("hyman", "abc"));
        str.append(" === ");
        str.append(CacheProvider.get("hyman"));
        str.append(" === ");
        str.append(CacheProvider.del("tyh"));

        str.append(" ======== ");

        Cookie cookie = new Cookie("aaa", "bbb");
        str.append(CacheProvider.set("cookie", cookie));
        str.append(" === ");
        str.append(CacheProvider.get("cookie", Cookie.class));
        str.append(" === ");
        str.append(CacheProvider.del("cookie"));

        return str.toString();
    }
}
