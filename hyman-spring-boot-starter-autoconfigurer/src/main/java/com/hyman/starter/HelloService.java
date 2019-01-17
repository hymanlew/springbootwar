package com.hyman.starter;

import org.springframework.beans.factory.annotation.Autowired;

public class HelloService {

    private HelloProperties properties;

    public HelloProperties getProperties() {
        return properties;
    }

    public void setProperties(HelloProperties properties) {
        this.properties = properties;
    }

    public String hello(String data){
        return properties.getPrefix()+"-"+data+"-"+properties.getSuffix();
    }
}
