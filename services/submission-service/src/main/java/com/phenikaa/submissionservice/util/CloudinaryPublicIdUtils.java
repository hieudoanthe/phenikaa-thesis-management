package com.phenikaa.submissionservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CloudinaryPublicIdUtils {

    public static String extractPublicId(String filePath) {
        if (filePath == null) return null;
        if (!filePath.contains("cloudinary.com") && !filePath.startsWith("http")) {
            return filePath;
        }
        try {
            String[] parts = filePath.split("/upload/");
            if (parts.length > 1) {
                String pathPart = parts[1];
                if (pathPart.startsWith("v")) {
                    int slashIndex = pathPart.indexOf("/");
                    if (slashIndex > 0) {
                        pathPart = pathPart.substring(slashIndex + 1);
                    }
                }
                int dotIndex = pathPart.lastIndexOf(".");
                if (dotIndex > 0) {
                    pathPart = pathPart.substring(0, dotIndex);
                }
                return pathPart;
            }
        } catch (Exception ignored) {}
        return filePath;
    }
}


