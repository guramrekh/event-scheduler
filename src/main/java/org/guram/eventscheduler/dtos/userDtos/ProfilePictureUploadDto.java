package org.guram.eventscheduler.dtos.userDtos;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ProfilePictureUploadDto(
        @NotBlank @URL String imageUrl
) {}
