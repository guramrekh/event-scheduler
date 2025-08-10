package org.guram.eventscheduler.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> getUploadSignature(Map<String, Object> paramsToSign) {
        long timestamp = System.currentTimeMillis() / 1000L;
        paramsToSign.put("timestamp", timestamp);

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        Map<String, Object> response = new HashMap<>();
        response.put("signature", signature);
        response.put("timestamp", timestamp);
        response.put("api_key", cloudinary.config.apiKey);

        return response;
    }

    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null)
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary: " + imageUrl, e);
        }
    }

    private String extractPublicIdFromUrl(String url) {
        Pattern pattern = Pattern.compile("/upload/(?:v\\d+/)?([^.]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find())
            return matcher.group(1);

        return null;
    }

}
