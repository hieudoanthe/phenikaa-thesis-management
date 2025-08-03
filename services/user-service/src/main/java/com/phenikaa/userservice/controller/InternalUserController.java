package com.phenikaa.userservice.controller;

import com.phenikaa.dto.request.LoginRequest;
import com.phenikaa.dto.response.UserInfoDTO;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    @PostMapping("/verify")
    public ResponseEntity<UserInfoDTO> verifyUser(@RequestBody LoginRequest request) {
        return userService.verifyUser(request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
