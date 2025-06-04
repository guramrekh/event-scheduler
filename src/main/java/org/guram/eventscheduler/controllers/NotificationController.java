package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.NotificationService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.guram.eventscheduler.controllers.Utils.getCurrentUser;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }


    @GetMapping()
    public ResponseEntity<List<NotificationResponseDto>> getNotificationsForUser(
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUser(userDetails, userService).getId();
        List<NotificationResponseDto> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

}
