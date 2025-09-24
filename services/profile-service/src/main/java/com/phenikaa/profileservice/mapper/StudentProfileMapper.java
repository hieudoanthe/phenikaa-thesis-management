package com.phenikaa.profileservice.mapper;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.profileservice.dto.request.UpdateStudentProfileRequest;
import com.phenikaa.profileservice.dto.response.GetStudentProfileResponse;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.StudentProfile;
import com.phenikaa.profileservice.entity.TeacherProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    StudentProfile dtoToStudentProfile(StudentProfile dto);

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "studentProfile.major", target = "major")
    @Mapping(source = "studentProfile.className", target = "className")
    @Mapping(source = "studentProfile.phoneNumber", target = "phoneNumber")
    @Mapping(source = "studentProfile.avt", target = "avt")

    @BeanMapping(ignoreByDefault = true)
    GetStudentProfileResponse toResponse(GetUserResponse user, StudentProfile studentProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStudentProfile(UpdateStudentProfileRequest updateStudentProfileRequest,@MappingTarget StudentProfile studentProfile);

}
