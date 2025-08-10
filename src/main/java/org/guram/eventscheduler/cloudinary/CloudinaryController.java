package org.guram.eventscheduler.cloudinary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
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
