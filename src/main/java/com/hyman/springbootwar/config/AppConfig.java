package com.hyman.springbootwar.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

// 在 boot 项目中不需要手动扫描，它会自动扫描。在 springmvc 中可以手动配置。
//@ComponentScan("com.hyman.springbootwar")
//@EnableJpaRepositories("com.hyman.springbootwar.dao")
@Configuration
@EnableTransactionManagement
public class AppConfig {

    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource dataSource(){
        return new DruidDataSource();
    }


    @Bean("entityManagerFactory")
    LocalContainerEntityManagerFactoryBean factoryBean(){
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan("com.hyman.spring.springbootwar.entity");

        //处理 jpa hibernate entity
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.MYSQL);
        jpaVendorAdapter.setShowSql(true);
        factoryBean.setJpaVendorAdapter(jpaVendorAdapter);

        // 设置其他属性
        //Properties factoryProperties = new Properties();
        //factoryProperties.put("hibernate.dialect","org.hibernate.dialect.MySQL5Dialect");
        //factoryBean.setJpaProperties(factoryProperties);

        return factoryBean;
    }

    @Bean("transactionManager")
    PlatformTransactionManager platformTransactionManager(){
        return new JpaTransactionManager(factoryBean().getObject());
    }

}
