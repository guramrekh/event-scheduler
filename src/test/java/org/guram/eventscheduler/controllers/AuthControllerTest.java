package org.guram.eventscheduler.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreateDto validUserCreateDto;

    @BeforeEach
    void setUp() {
        this.validUserCreateDto = new UserCreateDto("john", "wick", "john.wick@email.com", "password123");
    }


    @Test
    void registerUser_shouldReturn201Created_andLocationHeader_whenValidInput() throws Exception {
        var userResponseDto = new UserResponseDto(10L, "john", "wick", "john.wick@email.com", 
                null, null, 0, 0, 0, 0);
        
        when(userService.createUser(any(UserCreateDto.class))).thenReturn(userResponseDto);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/10"))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.email").value("john.wick@email.com"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("wick"));

        verify(userService).createUser(validUserCreateDto);
    }

    @Test
    void registerUser_shouldReturn400BadRequest_whenInvalidInput() throws Exception {
        var invalidUserDto = new UserCreateDto("", "wick", "not-an-email", "");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_shouldReturn400BadRequest_whenBlankFirstName() throws Exception {
        var invalidUserDto = new UserCreateDto("", "wick", "john.wick@email.com", "password123");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_shouldReturn400BadRequest_whenBlankLastName() throws Exception {
        var invalidUserDto = new UserCreateDto("john", "", "john.wick@email.com", "password123");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_shouldReturn400BadRequest_whenInvalidEmail() throws Exception {
        var invalidUserDto = new UserCreateDto("john", "wick", "invalid-email", "password123");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_shouldReturn400BadRequest_whenBlankPassword() throws Exception {
        var invalidUserDto = new UserCreateDto("john", "wick", "john.wick@email.com", "");

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());
    }

}