package com.phenikaa.profileservice.service.interfaces;

import com.phenikaa.dto.ProfileDto;
import com.phenikaa.profileservice.entity.StudentProfile;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileDto getSupervisorId(Integer id);
    StudentProfile createStudentProfile(StudentProfile createStudentProfileRequest, Integer userId, MultipartFile avtFile);
}
