package com.phenikaa.profileservice.controller;

import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.service.implement.CloudinaryServiceImpl;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile-service/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @PostMapping(value = "/create-profile-student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentProfile createProfileStudent(
            @RequestHeader("Authorization") String token,
            @RequestPart("profile") StudentProfile studentProfile,
            @RequestPart(value = "avtFile", required = false) MultipartFile avtFile) {

        Integer userId = jwtUtil.extractUserId(token);

        return profileService.createStudentProfile(studentProfile, userId, avtFile);
    }

}
