package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.request.LoginRequest;
import com.phenikaa.userservice.dto.response.UserInfoDTO;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class UserInternalController {

    private final UserService userService;


    @PostMapping("/verify")
    public Mono<UserInfoDTO> verifyUser(@RequestBody LoginRequest request) {
        return userService.verifyUser(request.username(), request.password());
    }
}
