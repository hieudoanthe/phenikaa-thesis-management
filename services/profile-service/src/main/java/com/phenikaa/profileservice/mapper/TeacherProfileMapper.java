package com.phenikaa.profileservice.mapper;

import com.phenikaa.dto.response.GetUserResponse;
import com.phenikaa.profileservice.dto.request.UpdateTeacherProfileRequest;
import com.phenikaa.profileservice.dto.response.GetTeacherProfileResponse;
import com.phenikaa.profileservice.entity.TeacherProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeacherProfileMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "teacherProfile.specialization", target = "specialization")
    @Mapping(source = "teacherProfile.degree", target = "degree")
    @Mapping(source = "teacherProfile.department", target = "department")
    @Mapping(source = "teacherProfile.phoneNumber", target = "phoneNumber")
    @Mapping(source = "teacherProfile.avt", target = "avt")

    @BeanMapping(ignoreByDefault = true)
    GetTeacherProfileResponse toResponse(GetUserResponse user, TeacherProfile teacherProfile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTeacherProfile(UpdateTeacherProfileRequest updateTeacherProfileRequestp, @MappingTarget TeacherProfile teacherProfile);
}
