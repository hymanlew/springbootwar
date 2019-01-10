package com.hyman.springbootwar.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * 当前项目由于配置了 hibernate.hbm2ddl.auto，所以在应用启动的时候 hibernate 框架会自动去数据库中创建对应的表。
 * persistence： 持续；固执；存留；
 *
 * 以下注解是 herbinate 框架要求必须要有的。
 * 使用 redis 处理对象，必须实现序列化接口进行序列化。
 */
@Entity
public class User implements Serializable{

    private static final long serialVersionUID = 113L;

    @Id
    @GeneratedValue
    private Integer id;

    // 判断设置不为空
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    public User() {
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public User(Integer id,String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
