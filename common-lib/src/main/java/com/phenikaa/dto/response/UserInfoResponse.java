package com.phenikaa.dto.response;


import java.util.List;

public record UserInfoResponse(Integer id, String username, List<String> roles) {}