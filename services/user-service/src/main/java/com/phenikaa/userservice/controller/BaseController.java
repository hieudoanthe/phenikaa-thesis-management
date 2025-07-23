package com.phenikaa.userservice.controller;


import com.phenikaa.common.dto.UserDto;
import com.phenikaa.userservice.service.implement.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class BaseController {
    private final UserServiceImpl userService;
    @GetMapping("/userId/{id}")
    public UserDto getUserById(@PathVariable("id") Integer id) {
        return userService.getUserById(id);
    }
}
