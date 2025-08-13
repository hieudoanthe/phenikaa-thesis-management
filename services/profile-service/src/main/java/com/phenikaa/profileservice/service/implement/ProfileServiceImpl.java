package com.phenikaa.profileservice.service.implement;

import com.phenikaa.dto.ProfileDto;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.mapper.StudentProfileMapper;
import com.phenikaa.profileservice.repository.StudentProfileRepository;
import com.phenikaa.profileservice.service.interfaces.CloudinaryService;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final StudentProfileMapper studentProfileMapper;
    private final StudentProfileRepository studentProfileRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public ProfileDto getSupervisorId(Integer id) {
        ProfileDto profile = new ProfileDto();
        profile.setSupervisorId(id);
        return profile;
    }

    @Override
    public StudentProfile createStudentProfile(StudentProfile createStudentProfileRequest, Integer userId, MultipartFile avtFile) {
        if (studentProfileRepository.existsById(userId)) {
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


}
