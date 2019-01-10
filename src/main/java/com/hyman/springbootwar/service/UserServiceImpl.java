package com.hyman.springbootwar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    /**
     * Spring的 JdbcTemplate是自动配置的，你可以直接使用 @Autowired 来注入到你自己的bean中来使用。
     * 它是一种基本的数据访问方式，结合构建 RESTful API 和使用 Thymeleaf 模板引擎渲染 Web视图的内容就已经可以完成 App服务
     * 端和 Web站点的开发任务了。
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create(String name, Integer age) {
        jdbcTemplate.update("INSERT INTO user(NAME ,age) VALUES (?,?)",name ,age);
    }

    @Override
    public void deleteByName(String name) {
        jdbcTemplate.update("DELETE FROM USER WHERE NAME = ?",name);
    }

    @Override
    public Integer getAllUsers() {
        return jdbcTemplate.queryForObject("select count(1) FROM user",Integer.class);
    }

    @Override
    public void changeUsers(String name,Integer age) {
        jdbcTemplate.update("UPDATE USER SET NAME = ? WHERE age = ?",name,age);
    }
}
