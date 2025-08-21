package org.guram.eventscheduler.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.guram.eventscheduler.dtos.userDtos.PasswordChangeDto;
import org.guram.eventscheduler.dtos.userDtos.ProfilePictureUploadDto;
import org.guram.eventscheduler.dtos.userDtos.UserProfileEditDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.security.CustomUserDetailsService;
import org.guram.eventscheduler.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        String authUserPassword = "<PASSWORD>";
        this.authenticatedUser = new User("john", "wick", authUserEmail, authUserPassword);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
                authUserEmail,
                authUserPassword,
                new ArrayList<>()
        );

        when(customUserDetailsService.loadUserByUsername(authUserEmail)).thenReturn(mockUserDetails);
        when(userService.getCurrentUser(any())).thenReturn(authenticatedUser);
    }


    @Test
    void getCurrentUserInfo_shouldReturnUserInfo_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(user(authUserEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.wick@email.com"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("wick"));

        verify(userService).getCurrentUser(any(UserDetails.class));
    }

    @Test
    void getCurrentUserInfo_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserByEmail_shouldReturnUser_whenEmailExists() throws Exception {
        String targetEmail = "joe.pesci@email.com";
        var responseDto = new UserResponseDto(1L, "joe", "pesci", targetEmail, null,
                null, 0, 0, 0, 0);

        when(userService.findUserByEmail(targetEmail)).thenReturn(responseDto);

        mockMvc.perform(get("/users/search")
                        .with(user(authUserEmail))
                        .param("email", targetEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value(targetEmail));

        verify(userService).findUserByEmail(targetEmail);
    }

    @Test
    void getUserByEmail_shouldReturn404NotFound_whenEmailNotExists() throws Exception {
        String nonExistentEmail = "someone@email.com";

        when(userService.findUserByEmail(nonExistentEmail)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/users/search")
                        .with(user(authUserEmail))
                        .param("email", nonExistentEmail))
                .andExpect(status().isNotFound());

        verify(userService).findUserByEmail(nonExistentEmail);
    }

    @Test
    void getUsersByName_shouldReturnUsersList_whenUsersAreFound() throws Exception {
        var userDto1 = new UserResponseDto(1L, "Joe", "Pesci", "joe.pesci@email.com", null,
                null, 0, 0, 0, 0);
        var userDto2 = new UserResponseDto(2L, "JoE", "PESCI", "joe.pesci.38@email.com", null,
                null, 0, 0, 0, 0);

        List<UserResponseDto> userList = List.of(userDto1, userDto2);

        when(userService.findUsersByName("joe", "pesci")).thenReturn(userList);

        mockMvc.perform(get("/users/search")
                        .with(user(authUserEmail))
                        .param("firstName", "joe")
                        .param("lastName", "pesci"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService).findUsersByName("joe", "pesci");
    }

    @Test
    void getUsersByName_shouldReturnEmptyList_whenUsersNotFound() throws Exception {
        when(userService.findUsersByName("non", "existent")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users/search")
                        .with(user(authUserEmail))
                        .param("firstName", "non")
                        .param("lastName", "existent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userService).findUsersByName("non", "existent");
    }

    @Test
    void deleteUser_shouldReturn204NoContent_whenAuthenticated() throws Exception {
        doNothing().when(userService).deleteUser(authenticatedUser);

        mockMvc.perform(delete("/users/delete")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(authenticatedUser);
    }

    @Test
    void deleteUser_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/users/delete")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void editUser_shouldSucceed_whenAuthenticatedAndValidInput() throws Exception {
        var editDto = new UserProfileEditDto("John", "Wick", "new bio");
        var responseDto = new UserResponseDto(1L, "John", "Wick", "john.wick@email.com", "new bio",
                null, 0, 0, 0, 0);

        when(userService.editUser(any(User.class), any(UserProfileEditDto.class))).thenReturn(responseDto);

        mockMvc.perform(put("/users/edit")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.wick@email.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Wick"))
                .andExpect(jsonPath("$.bio").value("new bio"));

        verify(userService).editUser(authenticatedUser, editDto);
    }

    @Test
    void editUser_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        var validDto = new UserProfileEditDto("John", "Wick", "Bio");

        mockMvc.perform(put("/users/edit")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void editUser_shouldReturn400BadRequest_whenInvalidInput() throws Exception {
        var invalidDto = new UserProfileEditDto("", "Wick", "Bio");

        mockMvc.perform(put("/users/edit")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void updateProfilePicture_shouldSucceed_whenAuthenticatedAndValidInput() throws Exception {
        var uploadDto = new ProfilePictureUploadDto("https://example.com/image.png");

        doNothing().when(userService).updateProfilePicture(any(User.class), any(ProfilePictureUploadDto.class));

        mockMvc.perform(put("/users/upload-picture")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uploadDto)))
                .andExpect(status().isOk());

        verify(userService).updateProfilePicture(authenticatedUser, uploadDto);
    }

    @Test
    void updateProfilePicture_shouldReturn400BadRequest_whenInputNotUrl() throws Exception {
        var invalidUploadDto = new ProfilePictureUploadDto("some-image");

        mockMvc.perform(put("/users/upload-picture")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUploadDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfilePicture_shouldReturn400BadRequest_whenInputBlank() throws Exception {
        var invalidUploadDto = new ProfilePictureUploadDto("");

        mockMvc.perform(put("/users/upload-picture")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUploadDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfilePicture_shouldReturn401Unauthorized_whenNotAuthenticated() throws Exception {
        var uploadDto = new ProfilePictureUploadDto("https://example.com/image.png");

        mockMvc.perform(put("/users/upload-picture")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(uploadDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeProfilePicture_shouldSucceed_whenAuthenticated() throws Exception {
        doNothing().when(userService).removeProfilePicture(any(User.class));

        mockMvc.perform(delete("/users/remove-picture")
                        .with(user(authUserEmail))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).removeProfilePicture(authenticatedUser);
    }

    @Test
    void removeProfilePicture_shouldReturn401Authorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/users/remove-picture")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldSucceed_whenAuthenticatedAndValidInput() throws Exception {
        var passwordDto = new PasswordChangeDto("<PASSWORD>", "<NEW_PASSWORD>", "<NEW_PASSWORD>");

        doNothing().when(userService).changePassword(any(User.class), any(PasswordChangeDto.class));

        mockMvc.perform(post("/users/change-password")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(authenticatedUser, passwordDto);
    }

    @Test
    void changePassword_shouldReturn409Conflict_whenIncorrectCurrentPassword() throws Exception {
        var passwordDto = new PasswordChangeDto("<PASSWORD123>", "<NEW_PASSWORD>", "<NEW_PASSWORD>");

        doThrow(ConflictException.class).when(userService).changePassword(any(User.class), any(PasswordChangeDto.class));

        mockMvc.perform(post("/users/change-password")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_shouldReturn409Conflict_whenNewPasswordIsSame() throws Exception {
        var passwordDto = new PasswordChangeDto("<PASSWORD>", "<PASSWORD>", "<PASSWORD>");

        doThrow(ConflictException.class).when(userService).changePassword(any(User.class), any(PasswordChangeDto.class));

        mockMvc.perform(post("/users/change-password")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_shouldReturn400BadRequest_whenBlankInput() throws Exception {
        var invalidDto = new PasswordChangeDto("<PASSWORD>", "", "");

        mockMvc.perform(post("/users/change-password")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturn400BadRequest_whenConfirmNewPasswordNotMatch() throws Exception {
        var invalidDto = new PasswordChangeDto("<PASSWORD>", "<NEW_PASSWORD>", "<NEW_PASSWORD_123>");

        mockMvc.perform(post("/users/change-password")
                        .with(user(authUserEmail))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

}