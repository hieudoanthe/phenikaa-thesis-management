package com.phenikaa.profileservice.mapper;

import com.phenikaa.profileservice.dto.request.CreateStudentProfileRequest;
import com.phenikaa.profileservice.entity.StudentProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    CreateStudentProfileRequest studentProfileToDto(StudentProfile studentProfile);

    StudentProfile dtoToStudentProfile(StudentProfile dto);

    void updateStudentProfileFromDto(CreateStudentProfileRequest dto, @MappingTarget StudentProfile entity);
}
