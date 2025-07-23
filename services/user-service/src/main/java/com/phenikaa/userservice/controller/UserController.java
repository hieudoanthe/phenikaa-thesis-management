package com.phenikaa.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/internal")
public class UserController {

    @GetMapping("/")
    public RedirectView showLoginPage() {
        return new RedirectView("");
    }
}