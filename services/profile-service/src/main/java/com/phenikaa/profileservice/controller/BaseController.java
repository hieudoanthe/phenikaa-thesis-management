package com.phenikaa.profileservice.controller;

import com.phenikaa.dto.ProfileDto;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ix")
@RequiredArgsConstructor
public class BaseController {
    private final ProfileService profileService;
    @GetMapping("/supervisorId/{id}")
    public ProfileDto getSupervisorId(@PathVariable("id") Integer id) {
        return profileService.getSupervisorId(id);
    }
}
