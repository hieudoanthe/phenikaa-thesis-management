package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    User save(User user);
}
