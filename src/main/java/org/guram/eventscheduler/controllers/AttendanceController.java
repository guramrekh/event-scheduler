package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.services.AttendanceService;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.guram.eventscheduler.controllers.Utils.getCurrentUser;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserService userService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }


    @PutMapping("/cancel")
    public ResponseEntity<AttendanceResponseDto> cancelAttendance(
                                @RequestParam Long eventId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        AttendanceResponseDto attendance = attendanceService.cancelAttendance(eventId, currentUserId);
        return ResponseEntity.ok(attendance);
    }

    @PutMapping("/mark-attended")
    public ResponseEntity<AttendanceResponseDto> markAttended(
                                @RequestParam Long eventId,
                                @RequestParam Long attendeeUserId,
                                @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getCurrentUser(userDetails, userService).getId();
        AttendanceResponseDto attendance = attendanceService.markAttended(eventId, currentUserId, attendeeUserId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AttendanceResponseDto>> listAttendancesByStatus(
                                @PathVariable Long eventId,
                                @RequestParam AttendanceStatus status) {
        List<AttendanceResponseDto> attendances = attendanceService.listAttendancesByStatus(eventId, status);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponseDto>> listUserRegistrations(@PathVariable Long userId) {
        List<AttendanceResponseDto> attendances = attendanceService.listUserRegistrations(userId);
        return ResponseEntity.ok(attendances);
    }
}
