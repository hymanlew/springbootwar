package com.hyman.springbootwar.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

// 索引库名，类型名。
//@Document(indexName = "hymanEm",type = "employee", shards = 1,replicas = 0, refreshInterval = "-1")
@Document(indexName = "hymanEm",type = "employee")
public class Employee {
    @Id
    private Integer id;
    //@Field
    private String firstName;
    //@Field
    private String lastName;
    //@Field
    private Integer age = 0;
    //@Field
    private String about;

    public Employee() {
    }

    public Employee(Integer id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                ", about='" + about + '\'' +
                '}';
    }
}
