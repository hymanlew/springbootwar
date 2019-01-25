package com.hyman.springbootwar.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * 加上了 @Document 注解之后，默认情况下这个实体中所有的属性都会被建立索引、并且分词。
 * 我们通过 @Field 注解来进行详细的指定，如果没有特殊需求，那么只需要添加 @Document 即可。
 */
// 索引库名，类型名（这两项必须为小写），默认分区数（默认5），每个分区默认的备份数（1），刷新间隔（1s），索引文件存储类型（fs）。
//@Document(indexName = "hymanEm",type = "employee", shards = 1,replicas = 0, refreshInterval = "1s", indexStoreType= "fs")
@Document(indexName = "hymanem",type = "employee")
public class Employee {

    // @Id注解加上后，在Elasticsearch里相应于该列就是主键了，在查询时就可以直接用主键查询。
    @Id
    private Integer id;

    /**
     * @Field 注解的属性设置：
     * type：FieldType.Auto，自动检测属性的类型。
     * index：FieldIndex.analyzed，默认情况下分词。
     *
     * format：DateFormat.none，默认不转换日期格式。
     * String pattern() default "";
     *
     * store：false，默认情况下不存储原文。
     * searchAnalyzer：default ""，指定字段搜索时使用的分词器。
     *
     * indexAnalyzer：default ""，指定字段建立索引时指定的分词器。
     * ignoreFields：default {}，忽略某个字段。
     * includeInParent：default false;
     */
    @Field
    private String firstName;
    @Field
    private String lastName;
    @Field
    private Integer age = 0;
    @Field
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
