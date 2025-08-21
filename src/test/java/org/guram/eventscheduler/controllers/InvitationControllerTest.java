package org.guram.eventscheduler.controllers;

import org.guram.eventscheduler.dtos.invitationDtos.InvitationResponseDto;
import org.guram.eventscheduler.models.InvitationStatus;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.security.CustomUserDetailsService;
import org.guram.eventscheduler.services.InvitationService;
import org.guram.eventscheduler.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvitationController.class)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InvitationService invitationService;

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
    void inviteUser_shouldReturn201CreatedAndLocationHeader_whenValidInput() throws Exception {
        Long inviterId = 1L;
        Long inviteeId = 2L;
        Long eventId = 3L;
        var invitationResponseDto = new InvitationResponseDto(5L, null, null, null,
                null, InvitationStatus.PENDING);
        
        when(invitationService.sendInvitation(inviterId, inviteeId, eventId)).thenReturn(invitationResponseDto);

        mockMvc.perform(post("/invitations/invite")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("eventId", "3")
                        .param("inviteeId", "2"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/invitations/5"))
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(invitationService).sendInvitation(inviterId, inviteeId, eventId);
    }

    @Test
    void inviteUser_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/invitations/invite")
                        .with(csrf())
                        .param("eventId", "3")
                        .param("inviteeId", "2"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void inviteUser_shouldReturn400BadRequest_whenMissingEventId() throws Exception {
        mockMvc.perform(post("/invitations/invite")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("inviteeId", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inviteUser_shouldReturn400BadRequest_whenMissingInviteeId() throws Exception {
        mockMvc.perform(post("/invitations/invite")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("eventId", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respondToInvitation_shouldReturn200Ok_whenAcceptedStatus() throws Exception {
        Long invitationId = 5L;
        var invitationResponseDto = new InvitationResponseDto(invitationId, null, null, null,
                null, InvitationStatus.ACCEPTED);
        
        when(invitationService.respondToInvitation(1L, invitationId, InvitationStatus.ACCEPTED))
                .thenReturn(invitationResponseDto);

        mockMvc.perform(put("/invitations/5/respond")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("newStatus", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invitationId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(invitationService).respondToInvitation(1L, invitationId, InvitationStatus.ACCEPTED);
    }

    @Test
    void respondToInvitation_shouldReturn200Ok_whenDeclinedStatus() throws Exception {
        Long invitationId = 5L;
        var invitationResponseDto = new InvitationResponseDto(invitationId, null, null,
                null, null, InvitationStatus.DECLINED);
        
        when(invitationService.respondToInvitation(1L, invitationId, InvitationStatus.DECLINED))
                .thenReturn(invitationResponseDto);

        mockMvc.perform(put("/invitations/5/respond")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("newStatus", "DECLINED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invitationId))
                .andExpect(jsonPath("$.status").value("DECLINED"));

        verify(invitationService).respondToInvitation(1L, invitationId, InvitationStatus.DECLINED);
    }

    @Test
    void respondToInvitation_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/invitations/5/respond")
                        .with(csrf())
                        .param("newStatus", "ACCEPTED"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void respondToInvitation_shouldReturn400BadRequest_whenMissingStatus() throws Exception {
        mockMvc.perform(put("/invitations/5/respond")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getInvitationsByStatus_shouldReturnInvitationsList_whenPendingStatus() throws Exception {
        var invitation1 = new InvitationResponseDto(5L, null, null, null, null, InvitationStatus.PENDING);
        var invitation2 = new InvitationResponseDto(6L, null, null, null, null, InvitationStatus.PENDING);
        var invitationsList = List.of(invitation1, invitation2);
        
        when(invitationService.listInvitationsReceivedByUserByStatus(authenticatedUser, InvitationStatus.PENDING))
                .thenReturn(invitationsList);

        mockMvc.perform(get("/invitations")
                        .with(user(authUserEmail))
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(6L))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(invitationService).listInvitationsReceivedByUserByStatus(authenticatedUser, InvitationStatus.PENDING);
    }

    @Test
    void getInvitationsByStatus_shouldReturnEmptyList_whenNoInvitations() throws Exception {
        when(invitationService.listInvitationsReceivedByUserByStatus(authenticatedUser, InvitationStatus.ACCEPTED))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/invitations")
                        .with(user(authUserEmail))
                        .param("status", "ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(invitationService).listInvitationsReceivedByUserByStatus(authenticatedUser, InvitationStatus.ACCEPTED);
    }

    @Test
    void getInvitationsByStatus_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/invitations")
                        .param("status", "PENDING"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getInvitationsByStatus_shouldReturnPendingInvitations_whenMissingStatusParameter() throws Exception {
        var invitation1 = new InvitationResponseDto(1L, null, null, null, null, InvitationStatus.PENDING);
        var pendingInvitationsList = List.of(invitation1);

        when(invitationService.listInvitationsReceivedByUserByStatus(authenticatedUser, null))
                .thenReturn(pendingInvitationsList);

        mockMvc.perform(get("/invitations")
                        .with(user(authUserEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

}