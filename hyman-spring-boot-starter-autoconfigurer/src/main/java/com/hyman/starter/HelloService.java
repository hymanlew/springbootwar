package com.hyman.starter;

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
