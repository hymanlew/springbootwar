package com.hyman.springbootwar.service;

import com.hyman.springbootwar.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    /**
     * 对于数据访问层，无论是 SQL,NOSQL，springboot默认都采用整合 springData 的方式进行统一处理，添加了大量自动配置，并屏蔽
     * 了很多设置。引入了各种 xxxTemplate，xxxRepository 来简化对数据访问层的操作，只需进行简单的设置即可。
     *
     * 默认是用 org.apache.tomcat.jdbc.pool.DataSource 作为数据源，其相关配置都在 DataSourceProperties 类里面。自动配置类：
     * org.springframework.boot.autoconfigure.jdbc 自动配置了DataSource，JdbcTemplate 来操作数据库。
     *
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

    @Override
    public List<User> getAll() {
        return jdbcTemplate.queryForList("select * FROM user",User.class);
    }
}
