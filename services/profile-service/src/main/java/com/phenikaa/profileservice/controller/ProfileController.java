package com.phenikaa.profileservice.controller;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateStudentProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateTeacherProfileRequest;
import com.phenikaa.profileservice.dto.response.GetStudentProfileResponse;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import com.phenikaa.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile-service")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/admin/create-profile-student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentProfile createProfileStudent(
            @RequestHeader("Authorization") String token,
            @RequestPart("profile") StudentProfile studentProfile,
            @RequestPart(value = "avtFile", required = false) MultipartFile avtFile) {

        Integer userId = jwtUtil.extractUserId(token);

        return profileService.createStudentProfile(studentProfile, userId, avtFile);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/create-profile")
    public ResponseEntity<Void> createProfile(@RequestBody CreateProfileRequest createProfileRequest) {
        profileService.createProfile(createProfileRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/delete-profile/{userId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Integer userId) {
        profileService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping(value = "/teacher/update-profile-teacher", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TeacherProfile updateProfileTeacher(
            @RequestHeader("Authorization") String token,
            @RequestPart("profile") UpdateTeacherProfileRequest updateRequest,
            @RequestPart(value = "avtFile", required = false) MultipartFile avtFile) {

        Integer userId = jwtUtil.extractUserId(token);

        return profileService.updateTeacherProfile(updateRequest, userId, avtFile);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/teacher/get-profile/{userId}")
    public ResponseEntity<GetTeacherProfileResponse> getTeacherProfile(@PathVariable Integer userId) {
        GetTeacherProfileResponse response = profileService.getTeacherProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @GetMapping("/student/get-profile/{userId}")
    public ResponseEntity<GetStudentProfileResponse> getStudentProfile(@PathVariable Integer userId) {
        GetStudentProfileResponse response = profileService.getStudentProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping(value = "/student/update-profile-student", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentProfile updateProfileStudent(
            @RequestHeader("Authorization") String token,
            @RequestPart("profile") UpdateStudentProfileRequest updateRequest,
            @RequestPart(value = "avtFile", required = false) MultipartFile avtFile) {

        Integer userId = jwtUtil.extractUserId(token);

        return profileService.updateStudentProfile(updateRequest, userId, avtFile);
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/student/get-all-teachers")
    public ResponseEntity<List<GetTeacherProfileResponse>> getAllTeacherProfiles() {
        List<GetTeacherProfileResponse> response = profileService.getAllTeacherProfiles();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/teacher/decrease-capacity")
    public ResponseEntity<Void> decreaseTeacherCapacity(@RequestHeader("Authorization") String token) {
        Integer userId = jwtUtil.extractUserId(token);
        profileService.decreaseTeacherCapacity(userId);
        return ResponseEntity.noContent().build();
    }

}
