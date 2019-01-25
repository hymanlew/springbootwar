package com.hyman.springbootwar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 使用外置的 Servlet 容器：
 *
 * 1，嵌入式Servlet容器可以将应用打成可执行的 jar包，优点是简单、便携，但缺点是默认不支持JSP、优化定制比较复杂（使用定制器 ServerProperties、
 * 	  需要自定义 EmbeddedServletContainerCustomizer，自定义嵌入式 Servlet容器的创建工厂 EmbeddedServletContainerFactory）。
 *
 * 2，外置的Servlet容器：外面安装Tomcat- 应用war包的方式打包：
 *
 *   1）、必须创建一个war项目（打包方式选 war，利用idea创建好目录结构），C+A+Shift+S（project structure）中 modules -- web 中设置 web
 *   resource（webapp文件夹），deployment（web.xml 文件路径）。
 *
 *   2）、添加 tomcat 容器（edit configuration），并设置 deployment war 包（与普通 war 项目完全一样），将嵌入式的Tomcat指定为provided。
 *   并且一定要使用 8.0 以上版本的 tomcat，否则无法启动。
 *
 * 启动原理：
 * jar包：执行SpringBoot主类的main方法，启动ioc容器，创建嵌入式的Servlet容器；
 * war包：启动服务器，服务器启动SpringBoot应用（SpringBootServletInitializer），启动ioc容器；
 */

/**
 * 当引入 redis-starter 后，容器中保存的是 RedisCashManager，并且它创建 rediscache 作为缓存组件。rediscache 通过操作 redis 来缓存数据。
 * 即一旦引入 redis-starter，则 springboot 就默认把 redis 当作缓存，它是全自动配置的。
 * 但其对象的保存都是使用 JDK 进行保存的。
 */

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class SpringbootwarApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootwarApplication.class, args);
	}
}

