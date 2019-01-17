package com.hyman.springbootwar.controller;

import com.hyman.springbootwar.dao.UserRepository;
import com.hyman.springbootwar.entity.User;
import com.hyman.springbootwar.service.UserService;
import com.hyman.starter.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
public class TestController {

    @Resource
    private UserService userService;
    @Resource
    private UserRepository userRepository;
    @Autowired
    private HelloService helloService;

    /**
     * http://localhost:8088/getUser/1
     *
     * http://localhost:8088/saveUser?name=li123&age=10&password=123
     *
     * http://localhost:8088/hyman/你好
     */

    @GetMapping("/getUser/{id}")
    @ResponseBody
    public User getUser(@PathVariable Integer id){
        User user =  userRepository.getOne(id);
        return user;
    }

    @GetMapping("/saveUser")
    @ResponseBody
    public User getUser(User user){
        User u =  userRepository.save(user);
        return u;
    }

    @GetMapping("/hyman/{data}")
    @ResponseBody
    public String sayHello(@PathVariable String data){
        return helloService.hello(data);
    }
}
