package org.guram.eventscheduler.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guram.eventscheduler.dtos.eventDtos.EventRequestDto;
import org.guram.eventscheduler.dtos.eventDtos.EventResponseDto;
import org.guram.eventscheduler.dtos.eventDtos.EventWithRoleDto;
import org.guram.eventscheduler.models.AttendanceRole;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.security.CustomUserDetailsService;
import org.guram.eventscheduler.services.EventService;
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
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

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
    void getEvents_shouldReturnEventsList_whenAuthenticated() throws Exception {
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(1L, "Team Meeting", "Weekly sync meeting",
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        var eventWithRoleDto = new EventWithRoleDto(eventResponseDto, AttendanceRole.ATTENDEE);
        
        when(eventService.getFilteredEventsWithRole(authenticatedUser, null, "UPCOMING", false))
                .thenReturn(List.of(eventWithRoleDto));

        mockMvc.perform(get("/events")
                        .with(user(authUserEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].event.id").value(1L))
                .andExpect(jsonPath("$[0].event.title").value("Team Meeting"));

        verify(eventService).getFilteredEventsWithRole(authenticatedUser, null, "UPCOMING", false);
    }

    @Test
    void getEvents_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEvent_shouldReturn201Created_andLocationHeader_whenValidInput() throws Exception {
        var eventDateTime = LocalDateTime.now().plusDays(2);
        var eventRequestDto = new EventRequestDto("Company Retreat", "Annual team building event",
                eventDateTime, "Mountain Resort");
        var eventResponseDto = new EventResponseDto(10L, "Company Retreat", "Annual team building event", 
                eventDateTime, "Mountain Resort", false, List.of(), List.of(), Map.of());
        
        when(eventService.createEvent(authenticatedUser, eventRequestDto)).thenReturn(eventResponseDto);

        mockMvc.perform(post("/events/create")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/events/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.title").value("Company Retreat"));

        verify(eventService).createEvent(authenticatedUser, eventRequestDto);
    }

    @Test
    void createEvent_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        var eventDateTime = LocalDateTime.now().plusDays(2);
        var validEventDto = new EventRequestDto("Company Retreat", "Annual team building event", 
                eventDateTime, "Mountain Resort");

        mockMvc.perform(post("/events/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEventDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void editEvent_shouldReturn200Ok_whenAuthenticatedAndValidInput() throws Exception {
        Long eventId = 10L;
        var eventDateTime = LocalDateTime.now().plusDays(3);
        var editRequestDto = new EventRequestDto("Updated Meeting", "Updated description", 
                eventDateTime, "New Location");
        var editResponseDto = new EventResponseDto(eventId, "Updated Meeting", "Updated description", 
                eventDateTime, "New Location", false, List.of(), List.of(), Map.of());
        
        when(eventService.editEvent(eventId, 1L, editRequestDto, true)).thenReturn(editResponseDto);

        mockMvc.perform(put("/events/10/edit")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.title").value("Updated Meeting"));

        verify(eventService).editEvent(eventId, 1L, editRequestDto, true);
    }

    @Test
    void editEvent_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        var eventDateTime = LocalDateTime.now().plusDays(3);
        var validEditDto = new EventRequestDto("Updated Meeting", "Updated description", 
                eventDateTime, "New Location");

        mockMvc.perform(put("/events/10/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEditDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelEvent_shouldReturn204NoContent_whenAuthenticated() throws Exception {
        Long eventId = 10L;

        mockMvc.perform(put("/events/10/cancel")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(eventService).cancelEvent(eventId, 1L);
    }

    @Test
    void cancelEvent_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/10/cancel")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addAsOrganizer_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 10L;
        Long newOrganizerUserId = 9L;
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(eventId, "Team Meeting", "Weekly sync meeting", 
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        
        when(eventService.makeAttendeeOrganizer(authenticatedUser, newOrganizerUserId, eventId))
                .thenReturn(eventResponseDto);

        mockMvc.perform(put("/events/10/add-organizer")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("newOrgUserId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService).makeAttendeeOrganizer(authenticatedUser, newOrganizerUserId, eventId);
    }

    @Test
    void addAsOrganizer_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/10/add-organizer")
                        .with(csrf())
                        .param("newOrgUserId", "9"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeAsOrganizer_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 10L;
        Long removeUserId = 9L;
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(eventId, "Team Meeting", "Weekly sync meeting", 
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        
        when(eventService.removeOrganizerRole(authenticatedUser, removeUserId, eventId))
                .thenReturn(eventResponseDto);

        mockMvc.perform(put("/events/10/remove-organizer")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("removeUserId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService).removeOrganizerRole(authenticatedUser, removeUserId, eventId);
    }

    @Test
    void removeAsOrganizer_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/10/remove-organizer")
                        .with(csrf())
                        .param("removeUserId", "9"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void kickOutAttendee_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 10L;
        Long removeUserId = 9L;
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(eventId, "Team Meeting", "Weekly sync meeting", 
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        
        when(eventService.kickUserFromEvent(authenticatedUser, removeUserId, eventId))
                .thenReturn(eventResponseDto);

        mockMvc.perform(put("/events/10/kickout-attendee")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("removeUserId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService).kickUserFromEvent(authenticatedUser, removeUserId, eventId);
    }

    @Test
    void kickOutAttendee_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/10/kickout-attendee")
                        .with(csrf())
                        .param("removeUserId", "9"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAttended_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 10L;
        Long attendeeUserId = 7L;
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(eventId, "Team Meeting", "Weekly sync meeting", 
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        
        when(eventService.markAttended(authenticatedUser, attendeeUserId, eventId))
                .thenReturn(eventResponseDto);

        mockMvc.perform(put("/events/mark-attended")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("eventId", "10")
                        .param("attendeeUserId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService).markAttended(authenticatedUser, attendeeUserId, eventId);
    }

    @Test
    void markAttended_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/mark-attended")
                        .with(csrf())
                        .param("eventId", "10")
                        .param("attendeeUserId", "7"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void markAllAttended_shouldReturn200Ok_whenAuthenticated() throws Exception {
        Long eventId = 10L;
        var eventDateTime = LocalDateTime.now().plusDays(1);
        var eventResponseDto = new EventResponseDto(eventId, "Team Meeting", "Weekly sync meeting", 
                eventDateTime, "Conference Room A", false, List.of(), List.of(), Map.of());
        
        when(eventService.markAllAttended(authenticatedUser, eventId)).thenReturn(eventResponseDto);

        mockMvc.perform(put("/events/mark-all-attended")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .param("eventId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId));

        verify(eventService).markAllAttended(authenticatedUser, eventId);
    }

    @Test
    void markAllAttended_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/events/mark-all-attended")
                        .with(csrf())
                        .param("eventId", "10"))
                .andExpect(status().isUnauthorized());
    }

}