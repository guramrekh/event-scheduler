package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.models.Attendance;
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
    public ResponseEntity<Attendance> inviteUser(
                                @RequestParam Long eventId,
                                @RequestParam Long actorUserId,
                                @RequestParam Long inviteeUserId) {
        Attendance attendance = attendanceService.inviteUser(eventId, actorUserId, inviteeUserId);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    @PutMapping("/respond")
    public ResponseEntity<Attendance> respondToInvitation(
                                @RequestParam Long eventId,
                                @RequestParam Long inviteeUserId,
                                @RequestParam AttendanceStatus newStatus) {
        Attendance attendance = attendanceService.respondToInvitation(eventId, inviteeUserId, newStatus);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @PutMapping("/cancel")
    public ResponseEntity<Attendance> cancelAttendance(
                                @RequestParam Long eventId,
                                @RequestParam Long userId) {
        Attendance attendance = attendanceService.cancelAttendance(eventId, userId);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @PutMapping("/mark-attended")
    public ResponseEntity<Attendance> markAttended(
                                @RequestParam Long eventId,
                                @RequestParam Long actorUserId,
                                @RequestParam Long attendeeUserId) {
        Attendance attendance = attendanceService.markAttended(eventId, actorUserId, attendeeUserId);
        return new ResponseEntity<>(attendance, HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Attendance>> listAttendancesByStatus(
                                @PathVariable Long eventId,
                                @RequestParam AttendanceStatus status) {
        List<Attendance> attendances = attendanceService.listAttendancesByStatus(eventId, status);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Attendance>> listUserRegistrations(@PathVariable Long userId) {
        List<Attendance> attendances = attendanceService.listUserRegistrations(userId);
        return new ResponseEntity<>(attendances, HttpStatus.OK);
    }
}
