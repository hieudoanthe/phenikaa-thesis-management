package com.phenikaa.authservice.dto.response;


import java.util.List;

public record UserInfoDTO(Integer id, String username, List<String> roles) {}