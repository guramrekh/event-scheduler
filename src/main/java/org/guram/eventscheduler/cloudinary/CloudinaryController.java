package org.guram.eventscheduler.cloudinary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController()
@RequestMapping("/cloudinary")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }


    @PostMapping("/upload/sign")
    public ResponseEntity<Map<String, Object>> getSignature(
            @RequestBody(required = false) Map<String, Object> paramsToSign) {
        Map<String, Object> finalParams = (paramsToSign != null) ? paramsToSign : new java.util.HashMap<>();
        Map<String, Object> uploadSignature = cloudinaryService.getUploadSignature(finalParams);
        return ResponseEntity.ok(uploadSignature);
    }
}
