package com.phenikaa.profileservice.service.implement;

import com.phenikaa.common.dto.ProfileDto;
import com.phenikaa.profileservice.service.interfaces.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    @Override
    public ProfileDto getSupervisorId(Integer id) {
        ProfileDto profile = new ProfileDto();
        profile.setSupervisorId(id);
        return profile;
    }
}
