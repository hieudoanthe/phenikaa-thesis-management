package com.phenikaa.dto.response;


import java.util.List;

public record AuthenticatedUserResponse(Integer id, String username, List<String> roles) {}