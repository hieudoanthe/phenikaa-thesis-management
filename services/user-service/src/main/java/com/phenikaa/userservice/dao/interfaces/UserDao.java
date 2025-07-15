package com.phenikaa.userservice.dao.interfaces;

import com.phenikaa.userservice.entity.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    User save(User user);
}
