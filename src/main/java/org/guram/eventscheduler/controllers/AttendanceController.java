package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.DTOs.attendanceDTOs.AttendanceResponseDto;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }


    @PutMapping("/cancel")
    public ResponseEntity<AttendanceResponseDto> cancelAttendance(
                                @RequestParam Long eventId,
                                @RequestParam Long userId) {
        AttendanceResponseDto attendance = attendanceService.cancelAttendance(eventId, userId);
        return ResponseEntity.ok(attendance);
    }

    @PutMapping("/mark-attended")
    public ResponseEntity<AttendanceResponseDto> markAttended(
                                @RequestParam Long eventId,
                                @RequestParam Long actorUserId,
                                @RequestParam Long attendeeUserId) {
        AttendanceResponseDto attendance = attendanceService.markAttended(eventId, actorUserId, attendeeUserId);
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
