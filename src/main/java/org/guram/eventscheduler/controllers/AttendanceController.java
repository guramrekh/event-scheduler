package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.AttendanceService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }


    @PutMapping("/withdraw")
    public ResponseEntity<AttendanceResponseDto> withdrawFromEvent(
                                @RequestParam Long eventId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        AttendanceResponseDto attendance = attendanceService.withdrawFromEvent(currentUser, eventId);
        return ResponseEntity.ok(attendance);
    }

}
