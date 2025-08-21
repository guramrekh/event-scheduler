package org.guram.eventscheduler.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guram.eventscheduler.dtos.notificationDtos.NotificationResponseDto;
import org.guram.eventscheduler.models.NotificationType;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.security.CustomUserDetailsService;
import org.guram.eventscheduler.services.NotificationService;
import org.guram.eventscheduler.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private User authenticatedUser;
    private final String authUserEmail = "john.wick@email.com";

    @BeforeEach
    void setUp() {
        String authUserPassword = "password123";
        this.authenticatedUser = new User("john", "wick", authUserEmail, authUserPassword);
        authenticatedUser.setId(3L);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                authUserEmail,
                authUserPassword,
                new ArrayList<>()
        );

        when(customUserDetailsService.loadUserByUsername(authUserEmail)).thenReturn(mockUserDetails);
        when(userService.getCurrentUser(any())).thenReturn(authenticatedUser);
    }

    @Test
    void getNotificationsForUser_shouldReturnNotificationsList_whenAuthenticated() throws Exception {
        var notification1 = new NotificationResponseDto(1L, null,"Event cancelled", NotificationType.EVENT_CANCELLED, LocalDateTime.now(), false);
        var notification2 = new NotificationResponseDto(2L, null, "Event update", NotificationType.EVENT_DETAILS_UPDATED, LocalDateTime.now(), false);
        var notificationsList = List.of(notification1, notification2);
        
        when(notificationService.getNotificationsForUser(eq(authenticatedUser))).thenReturn(notificationsList);

        mockMvc.perform(get("/notifications")
                        .with(user(authUserEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].message").value("Event cancelled"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].message").value("Event update"))
                .andExpect(jsonPath("$[1].read").value(false));

        verify(notificationService).getNotificationsForUser(authenticatedUser);
    }

    @Test
    void getNotificationsForUser_shouldReturnEmptyList_whenNoNotifications() throws Exception {
        when(notificationService.getNotificationsForUser(eq(authenticatedUser))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/notifications")
                        .with(user(authUserEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(notificationService).getNotificationsForUser(authenticatedUser);
    }

    @Test
    void getNotificationsForUser_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAsRead_shouldReturn204NoContent_whenAuthenticated() throws Exception {
        Long notificationId = 10L;

        mockMvc.perform(put("/notifications/10/mark-as-read")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markNotificationAsRead(notificationId, authenticatedUser);
    }

    @Test
    void markAsRead_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/notifications/10/mark-as-read")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAllAsRead_shouldReturn204NoContent_whenAuthenticated() throws Exception {
        mockMvc.perform(put("/notifications/mark-all-as-read")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllNotificationAsRead(authenticatedUser);
    }

    @Test
    void markAllAsRead_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/notifications/mark-all-as-read")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}