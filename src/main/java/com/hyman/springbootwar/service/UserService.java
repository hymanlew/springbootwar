package com.hyman.springbootwar.service;

public interface UserService {

    void create(String name, Integer age);
    void deleteByName(String name);
    Integer getAllUsers();
    void changeUsers(String name, Integer age);
}
