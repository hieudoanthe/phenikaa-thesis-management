package com.phenikaa.userservice.controller;

import com.phenikaa.userservice.dto.request.UserRequest;
import com.phenikaa.userservice.entity.User;
import com.phenikaa.userservice.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/saveUser")
    public ResponseEntity<User> saveUser(@RequestBody UserRequest userRequest) {
        User savedUser = userService.saveUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}