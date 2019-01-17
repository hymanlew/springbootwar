package com.hyman.springbootwar.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 当前项目由于配置了 JPA hibernate.ddl-auto，所以在应用启动的时候 hibernate 框架会自动去数据库中创建对应的表。persistence（持续；固执；存留）；
 *
 * @Entity 注解是 JPA herbinate 必须要有的，作用是声明一个实体类并与数据库表映射。也可以指定一个表映射 @Table。不写默认就是当前类名的小写，即 user。
 *
 * hibernate 会给每个被管理的 pojo 加入一个 hibernateLazyInitializer属性（并且 struts-jsonplugin 或者其他的 jsonplugin 都是）。而且 jsonplugin
 * 用的是 java的内审机制，jsonplugin 通过 java 的反射机制将 pojo 转换成 json，会把 hibernateLazyInitializer 也拿出来操作,但是 hibernateLazyInitializer
 * 无法由反射得到，所以就抛异常了。
 *
 * @JsonIgnoreProperties(value={“xxx”}) 注解是必须要加在 pojo 类上的，value 值就是要忽略的一些属性，这些属性是被 lazy加载的，也就是many-to-one的 one
 * 端的 pojo上。
 * 以下注解的作用是告诉 jsonplug 组件，在将代理对象转换为 json 对象时，忽略value对应的数组中的属性，即：通过 java的反射机制将 pojo转换成 json的，属性，
 * 通过 java的反射机制将 pojo转换成 json的控制器。
 *
 * 如果你想在转换的时候继续忽略其他属性，可以在数组中继续加入。
 *
 * 使用 redis 处理对象，必须实现序列化接口进行序列化。
 */

@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
@Entity
@Table(name = "user")
public class User implements Serializable{

    private static final long serialVersionUID = 113L;

    /**
     * id，是声明此属性为主键。GeneratedValue，是主键的生成策略。IDENTITY 为自增。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 判断设置不为空，也可以指定对应的列名，name = "xxx"，字符长度 length = xx。
    @Column(nullable = false,name = "name",length = 10)
    private String name;

    // 当不指定列名时，则默认属性名，列名是相同的。
    @Column(nullable = false)
    private Integer age;

    @Column
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
