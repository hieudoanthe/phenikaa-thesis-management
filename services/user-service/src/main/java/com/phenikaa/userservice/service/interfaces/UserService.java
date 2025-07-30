package com.phenikaa.userservice.service.interfaces;

import com.phenikaa.userservice.dto.request.UserRequest;
import com.phenikaa.userservice.entity.User;

import java.util.Optional;

public interface UserService {
    User saveUser(UserRequest userRequest);
}
