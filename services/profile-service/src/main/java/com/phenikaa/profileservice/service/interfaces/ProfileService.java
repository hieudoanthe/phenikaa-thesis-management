package com.phenikaa.profileservice.service.interfaces;

import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateStudentProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateTeacherProfileRequest;
import com.phenikaa.profileservice.dto.response.GetStudentProfileResponse;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProfileService {
    StudentProfile createStudentProfile(StudentProfile createStudentProfileRequest, Integer userId, MultipartFile avtFile);
    void createProfile(CreateProfileRequest createProfileRequest);
    GetStudentProfileResponse getStudentProfile(Integer userId);
    GetTeacherProfileResponse getTeacherProfile(Integer userId);
    StudentProfile updateStudentProfile(UpdateStudentProfileRequest request, Integer userId, MultipartFile avtFile);
    TeacherProfile updateTeacherProfile(UpdateTeacherProfileRequest request, Integer userId, MultipartFile avtFile);
    void deleteProfile(Integer userId);
    List<GetTeacherProfileResponse> getAllTeacherProfiles();
    void decreaseTeacherCapacity(Integer userId);
    
    // Statistics methods
    Long getProfileCount();
    Long getStudentProfileCount();
    Long getLecturerProfileCount();
    List<Map<String, Object>> getProfilesByMajor(String major);
    List<Map<String, Object>> getProfilesByYear(Integer year);
    List<Map<String, Object>> getStudentProfilesBySupervisor(Integer supervisorId);
    Map<String, Object> getProfileByUserId(Integer userId);
}
