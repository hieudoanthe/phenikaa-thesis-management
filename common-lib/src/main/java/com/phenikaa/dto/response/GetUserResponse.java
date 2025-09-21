package com.phenikaa.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class GetUserResponse {
    private Integer userId;
    private String fullName;
    private String username;
    private Integer status;
    private List<Integer> roleIds;
    private String periodDescription;
    private List<Integer> periodIds;
    private Integer totalRegistrations;
}
