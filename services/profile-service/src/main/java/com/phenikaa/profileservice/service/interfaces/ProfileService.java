package com.phenikaa.profileservice.service.interfaces;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateStudentProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateTeacherProfileRequest;
import com.phenikaa.profileservice.dto.response.GetStudentProfileResponse;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    StudentProfile createStudentProfile(StudentProfile createStudentProfileRequest, Integer userId, MultipartFile avtFile);
    void createProfile(CreateProfileRequest createProfileRequest);
    GetStudentProfileResponse getStudentProfile(Integer userId);
    GetTeacherProfileResponse getTeacherProfile(Integer userId);
    StudentProfile updateStudentProfile(UpdateStudentProfileRequest request, Integer userId, MultipartFile avtFile);
    TeacherProfile updateTeacherProfile(UpdateTeacherProfileRequest request, Integer userId, MultipartFile avtFile);
    void deleteProfile(Integer userId);
}
