package com.phenikaa.profileservice.service.implement;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.profileservice.client.UserServiceClient;
import com.phenikaa.dto.request.CreateProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateStudentProfileRequest;
import com.phenikaa.profileservice.dto.request.UpdateTeacherProfileRequest;
import com.phenikaa.profileservice.dto.response.GetStudentProfileResponse;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import com.phenikaa.profileservice.mapper.StudentProfileMapper;
import com.phenikaa.profileservice.mapper.TeacherProfileMapper;
import com.phenikaa.profileservice.repository.StudentProfileRepository;
import com.phenikaa.profileservice.repository.TeacherProfileRepository;
import com.phenikaa.profileservice.service.interfaces.CloudinaryService;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileMapper studentProfileMapper;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final CloudinaryService cloudinaryService;
    private final UserServiceClient userServiceClient;
    private final TeacherProfileMapper teacherProfileMapper;

    @Override
    public StudentProfile createStudentProfile(StudentProfile createStudentProfileRequest, Integer userId, MultipartFile avtFile) {
        if (studentProfileRepository.existsById(String.valueOf(userId))) {
            throw new IllegalStateException("Profile for userId " + userId + " already exists.");
        }

        StudentProfile studentProfile = studentProfileMapper.dtoToStudentProfile(createStudentProfileRequest);
        studentProfile.setUserId(userId);

        if (avtFile != null && !avtFile.isEmpty()) {
            String avtUrl = cloudinaryService.uploadFile(avtFile, "student_avatars");
            studentProfile.setAvt(avtUrl);
        }

        return studentProfileRepository.save(studentProfile);
    }


    @Override
    public void createProfile(CreateProfileRequest createProfileRequest) {
        if ("STUDENT".equalsIgnoreCase(createProfileRequest.getRoleName())) {
            if (!studentProfileRepository.existsByUserId(createProfileRequest.getUserId())) {
                StudentProfile profile = new StudentProfile();
                profile.setUserId(createProfileRequest.getUserId());
                studentProfileRepository.save(profile);
            }
        } else if ("TEACHER".equalsIgnoreCase(createProfileRequest.getRoleName())) {
            if (!teacherProfileRepository.existsByUserId(createProfileRequest.getUserId())) {
                TeacherProfile profile = new TeacherProfile();
                profile.setUserId(createProfileRequest.getUserId());
                teacherProfileRepository.save(profile);
            }
        }
    }

    @Override
    public GetStudentProfileResponse getStudentProfile(Integer userId) {

        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for userId=" + userId));

        GetUserResponse user = userServiceClient.getUserById(userId);

        return studentProfileMapper.toResponse(user, profile);
    }

    @Override
    public GetTeacherProfileResponse getTeacherProfile(Integer userId) {

        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for userId=" + userId));
        GetUserResponse user = userServiceClient.getUserById(userId);

        return teacherProfileMapper.toResponse(user, profile);
    }

    @Override
    @Transactional
    public StudentProfile updateStudentProfile(UpdateStudentProfileRequest request, Integer userId, MultipartFile avtFile) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for userId=" + userId));

        studentProfileMapper.updateStudentProfile(request, profile);

        profile.setUserId(userId);

        if (avtFile != null && !avtFile.isEmpty()) {
            String avtUrl = cloudinaryService.uploadFile(avtFile, "student_avatars");
            profile.setAvt(avtUrl);
        }

        return studentProfileRepository.save(profile);
    }

    @Override
    public TeacherProfile updateTeacherProfile(UpdateTeacherProfileRequest request, Integer userId, MultipartFile avtFile) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for userId=" + userId));

        teacherProfileMapper.updateTeacherProfile(request, profile);

        profile.setUserId(userId);

        if (avtFile != null && !avtFile.isEmpty()) {
            String avtUrl = cloudinaryService.uploadFile(avtFile, "teacher_avatars");
            profile.setAvt(avtUrl);
        }
        return teacherProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(Integer userId) {

        if (studentProfileRepository.existsByUserId(userId)) {
            studentProfileRepository.deleteByUserId(userId);
            return;
        }

        if (teacherProfileRepository.existsByUserId(userId)) {
            teacherProfileRepository.deleteByUserId(userId);
            return;
        }
        throw new IllegalArgumentException("Profile not found for userId=" + userId);
    }

    @Override
    public List<GetTeacherProfileResponse> getAllTeacherProfiles() {
        List<TeacherProfile> profiles = teacherProfileRepository.findAll();

        List<Integer> userIds = profiles.stream()
                .map(TeacherProfile::getUserId)
                .toList();
        List<GetUserResponse> users = userServiceClient.getUsersByIds(userIds);

        Map<Integer, GetUserResponse> userMap = users.stream()
                .collect(Collectors.toMap(GetUserResponse::getUserId, u -> u));

        return profiles.stream()
                .map(profile -> teacherProfileMapper.toResponse(
                        userMap.get(profile.getUserId()),
                        profile
                ))
                .toList();
    }


    // Statistics methods implementation
    @Override
    public Long getProfileCount() {
        return studentProfileRepository.count() + teacherProfileRepository.count();
    }

    @Override
    public Long getStudentProfileCount() {
        return studentProfileRepository.count();
    }

    @Override
    public Long getLecturerProfileCount() {
        return teacherProfileRepository.count();
    }

    @Override
    public List<Map<String, Object>> getProfilesByMajor(String major) {
        return studentProfileRepository.findByMajor(major).stream()
                .map(profile -> {
                    Map<String, Object> profileMap = new HashMap<>();
                    profileMap.put("userId", profile.getUserId());
                    profileMap.put("studentId", profile.getStudentId());
                    profileMap.put("major", profile.getMajor());
                    profileMap.put("supervisorId", null); // Not available in current entity
                    profileMap.put("createdAt", null); // Not available in current entity
                    return profileMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getStudentProfilesBySupervisor(Integer supervisorId) {
        // Note: StudentProfile entity doesn't have supervisorId field
        // This method returns empty list as supervisor information is not available
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getProfileByUserId(Integer userId) {
        StudentProfile studentProfile = studentProfileRepository.findByUserId(userId).orElse(null);
        if (studentProfile != null) {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("type", "STUDENT");
            profileMap.put("userId", studentProfile.getUserId());
            profileMap.put("studentId", studentProfile.getStudentId());
            profileMap.put("major", studentProfile.getMajor());
            profileMap.put("supervisorId", null);
            profileMap.put("createdAt", null);
            return profileMap;
        }

        // Try teacher profile
        TeacherProfile teacherProfile = teacherProfileRepository.findByUserId(userId).orElse(null);
        if (teacherProfile != null) {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put("type", "TEACHER");
            profileMap.put("userId", teacherProfile.getUserId());
            profileMap.put("createdAt", null);
            return profileMap;
        }

        return new HashMap<>();
    }

}
