package com.suraj.Customer_Portal_29.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "quality", "auto:good",
                            "fetch_format", "auto",
                            "width", 800,
                            "crop", "limit"
                    )
            );
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }
    public void deleteFile(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    public void deleteFiles(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;
        for (String url : imageUrls) {
            deleteFile(url);
        }
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            String[] versionAndId = afterUpload.split("/", 2);
            if (versionAndId.length < 2) return null;
            String publicIdWithExt = versionAndId[1];
            int dotIndex = publicIdWithExt.lastIndexOf(".");
            return dotIndex > 0 ? publicIdWithExt.substring(0, dotIndex) : publicIdWithExt;
        } catch (Exception e) {
            return null;
        }
    }
}