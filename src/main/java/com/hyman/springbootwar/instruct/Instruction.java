package com.hyman.springbootwar.instruct;

public class Instruction {

    /**
     * Spring Boot工程结构推荐：
     * Spring Boot框架本身并没有对工程结构有特别的要求，但是按照最佳实践的工程结构可以帮助我们减少可能会遇见的坑，尤其是Spring
     * 包扫描机制的存在，如果您使用最佳实践的工程结构，可以免去不少特殊的配置工作。
     *
     * 典型示例：
     * root package结构：com.example.myproject;
     *
     * 应用主类 Application.java置于 root package下，通常我们会在应用主类中做一些框架配置扫描等配置，我们放在 root package下
     * 可以帮助程序减少手工配置来加载到我们希望被 Spring加载的内容;
     *
     * 实体（Entity）与数据访问层（Repository）置于 com.example.myproject.domain包下;
     * 逻辑层（Service）置于com.example.myproject.service包下;
     * Web层（web）置于com.example.myproject.web包下;
     *
     * com
        +- example
            +- myproject
                 +- Application.java
                 |
                 +- domain
                 |  +- Customer.java
                 |  +- CustomerRepository.java
                 |
                 +- service
                 |  +- CustomerService.java
                 |
                 +- web
                 |  +- CustomerController.java
                 |
     *
     * 这样的目的，是使 spring boot 默认扫包，看看是否可以去掉一些 @Configuration配置？
     *
     *
     * Spring Data 项目的目的是为了简化构建基于 Spring 框架应用的数据访问技术，包括非关系数据库、Map-Reduce 框架、云数据服务等等；
     * 另外也包含对关系数据库的访问支持。它包含了多个子项目：JPA，mongoDB，REST，redis 等等。
     *
     * 1、SpringData特点：它提供了使用统一的API来对数据访问层进行操作；这主要是Spring Data Commons项目来实现的。此项目让我们在使用
     *    关系型或者非关系型数据访问技术时都基于Spring提供的统一标准，标准包含了CRUD（创建、获取、更新、删除）、查询、排序和分页的相
     *    关操作。
     *
     * 2、统一的Repository接口（顺序是从父到子。在使用时只需实现这个接口即可，即面向 springData 编程）：
     *    Repository<T, ID extends Serializable>：统一接口。
     *    RevisionRepository<T, ID extends Serializable, N extends Number & Comparable<N>>：基于乐观锁机制。
     *    CrudRepository<T, ID extends Serializable>：基本CRUD操作。
     *    PagingAndSortingRepository<T, ID extends Serializable>：基本CRUD及分页。
     *    JpaRepository
     *
     * 3、提供数据访问模板类 xxxTemplate：如 MongoTemplate、RedisTemplate等。
     *
     * 4、JPA与Spring Data：
     *   1）、JpaRepository基本功能：编写接口继承JpaRepository既有crud及分页等基本功能。
     *   2）、定义符合规范的方法命名：在接口中只需要声明符合规范的方法，即拥有对应的功能。
     *   3）、@Query自定义查询，定制查询SQL。
     *   4）、Specifications查询（Spring Data JPA支持JPA2.0的Criteria查询）。
     *
     *
     *
     *
     */
}
