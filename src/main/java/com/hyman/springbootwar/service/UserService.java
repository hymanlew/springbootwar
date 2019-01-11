package com.hyman.springbootwar.service;

import com.hyman.springbootwar.entity.User;

import java.util.List;

public interface UserService {

    void create(String name, Integer age);
    void deleteByName(String name);
    Integer getAllUsers();
    void changeUsers(String name, Integer age);
    List<User> getAll();
}
