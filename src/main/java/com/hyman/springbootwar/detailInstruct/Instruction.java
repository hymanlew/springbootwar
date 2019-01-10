package com.hyman.springbootwar.detailInstruct;

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
     */
}
