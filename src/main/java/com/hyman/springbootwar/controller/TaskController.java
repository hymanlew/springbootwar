package com.hyman.springbootwar.controller;

import com.hyman.springbootwar.service.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
