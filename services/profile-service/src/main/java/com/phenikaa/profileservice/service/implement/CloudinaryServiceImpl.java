package com.phenikaa.profileservice.service.implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.phenikaa.profileservice.service.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "folder", folderName // chỉ định folder
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Upload ảnh thất bại", e);
        }
    }
}


