package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.DTOs.attendanceDTOs.AttendanceResponseDto;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    @PostMapping("/invite")
    public ResponseEntity<AttendanceResponseDto> inviteUser(
                                @RequestParam Long eventId,
                                @RequestParam Long actorUserId,
                                @RequestParam Long inviteeUserId) {
        AttendanceResponseDto attendance = attendanceService.inviteUser(eventId, actorUserId, inviteeUserId);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    @PutMapping("/respond")
    public ResponseEntity<AttendanceResponseDto> respondToInvitation(
                                @RequestParam Long eventId,
                                @RequestParam Long inviteeUserId,
                                @RequestParam AttendanceStatus newStatus) {
        AttendanceResponseDto attendance = attendanceService.respondToInvitation(eventId, inviteeUserId, newStatus);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @PutMapping("/cancel")
    public ResponseEntity<AttendanceResponseDto> cancelAttendance(
                                @RequestParam Long eventId,
                                @RequestParam Long userId) {
        AttendanceResponseDto attendance = attendanceService.cancelAttendance(eventId, userId);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @PutMapping("/mark-attended")
    public ResponseEntity<AttendanceResponseDto> markAttended(
                                @RequestParam Long eventId,
                                @RequestParam Long actorUserId,
                                @RequestParam Long attendeeUserId) {
        AttendanceResponseDto attendance = attendanceService.markAttended(eventId, actorUserId, attendeeUserId);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<AttendanceResponseDto>> listAttendancesByStatus(
                                @PathVariable Long eventId,
                                @RequestParam AttendanceStatus status) {
        List<AttendanceResponseDto> attendances = attendanceService.listAttendancesByStatus(eventId, status);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceResponseDto>> listUserRegistrations(@PathVariable Long userId) {
        List<AttendanceResponseDto> attendances = attendanceService.listUserRegistrations(userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }
}
