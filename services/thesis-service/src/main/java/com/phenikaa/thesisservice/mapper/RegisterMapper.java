package com.phenikaa.thesisservice.mapper;


import com.phenikaa.thesisservice.dto.request.RegisterTopicRequest;
import com.phenikaa.thesisservice.entity.Register;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterMapper {

    @Mapping(target = "registerId", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "registerStatus", constant = "PENDING")
    @Mapping(target = "registeredAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "projectTopic", ignore = true)
    Register toRegister(RegisterTopicRequest dto);
}