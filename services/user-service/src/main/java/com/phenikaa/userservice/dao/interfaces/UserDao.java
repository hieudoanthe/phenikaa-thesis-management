package com.phenikaa.userservice.dao.interfaces;

import com.phenikaa.userservice.dto.request.UserRequest;
import com.phenikaa.userservice.entity.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
}
