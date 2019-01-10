package com.hyman.springbootwar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 如果使用外置 server 容器时，需要修改启动类。并且一定要使用 8.0 以上版本的 tomcat。
 */
@SpringBootApplication
public class SpringbootwarApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootwarApplication.class, args);
	}
}

