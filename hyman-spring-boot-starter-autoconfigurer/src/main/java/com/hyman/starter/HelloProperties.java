package com.hyman.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 从全局配置文件中获取值（可以从主项目的配置文件中，也可以从本项目的配置文件中），并将本类生成 HelloProperties 类
 */
@ConfigurationProperties(prefix = "hyman.hello")
public class HelloProperties {

    private String prefix;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
