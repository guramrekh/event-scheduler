package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.attendanceDtos.AttendanceResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventSummaryDto;
import org.guram.eventscheduler.dtos.userDtos.UserSummaryDto;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.AttendanceStatus;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.security.CustomUserDetailsService;
import org.guram.eventscheduler.services.AttendanceService;
import org.guram.eventscheduler.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User authenticatedUser;
    private final String authUserEmail = "john.wick@email.com";

    @BeforeEach
    void setUp() {
        String authUserPassword = "password123";
        this.authenticatedUser = new User("john", "wick", authUserEmail, authUserPassword);
        authenticatedUser.setId(1L);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                authUserEmail,
                authUserPassword,
                new ArrayList<>()
        );

        when(customUserDetailsService.loadUserByUsername(authUserEmail)).thenReturn(mockUserDetails);
        when(userService.getCurrentUser(any())).thenReturn(authenticatedUser);
    }

    @Test
    void withdrawFromEvent_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 5L;
        var userSummary = new UserSummaryDto(1L, "john", "wick", "john.wick@email.com", null, null);
        var eventSummary = new EventSummaryDto(eventId, "Team Meeting", "Weekly sync meeting", 
                LocalDateTime.now().plusDays(1), "Conference Room A", false);
        var attendanceResponseDto = new AttendanceResponseDto(1L, userSummary, eventSummary, AttendanceStatus.WITHDRAWN, AttendanceRole.ATTENDEE);
        
        when(attendanceService.withdrawFromEvent(authenticatedUser, eventId)).thenReturn(attendanceResponseDto);

        mockMvc.perform(put("/attendances/withdraw")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.event.id").value(eventId))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"));

        verify(attendanceService).withdrawFromEvent(authenticatedUser, eventId);
    }

    @Test
    void withdrawFromEvent_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/attendances/withdraw")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", "5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withdrawFromEvent_shouldReturn400BadRequest_whenMissingEventId() throws Exception {
        mockMvc.perform(put("/attendances/withdraw")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdrawFromEvent_shouldReturn400BadRequest_whenInvalidEventId() throws Exception {
        mockMvc.perform(put("/attendances/withdraw")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("eventId", "invalid"))
                .andExpect(status().isBadRequest());
    }

}