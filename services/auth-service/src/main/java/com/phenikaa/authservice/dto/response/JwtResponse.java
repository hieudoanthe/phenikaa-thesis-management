package com.phenikaa.authservice.dto.response;


import java.util.List;

public record JwtResponse(String accessToken, String refreshToken, String username, List<String> roles) {}