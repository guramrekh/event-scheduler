package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.userDtos.PasswordChangeDto;
import org.guram.eventscheduler.dtos.userDtos.ProfilePictureUploadDto;
import org.guram.eventscheduler.dtos.userDtos.UserProfileEditDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import static org.guram.eventscheduler.utils.EntityToDtoMappings.mapUserToResponseDto;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        UserResponseDto user = mapUserToResponseDto(currentUser);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/search", params = "email")
    public ResponseEntity<UserResponseDto> getUserByEmail(@RequestParam String email) {
        UserResponseDto user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping(path = "/search", params = {"firstName", "lastName"})
    public ResponseEntity<List<UserResponseDto>> getUsersByName(@RequestParam String firstName,
                                                              @RequestParam String lastName) {
        var users = userService.findUsersByName(firstName, lastName);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        userService.deleteUser(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/edit")
    public ResponseEntity<UserResponseDto> editUser(@AuthenticationPrincipal UserDetails userDetails,
                                                      @Valid @RequestBody UserProfileEditDto userProfileEditDto) {
        User currentUser = userService.getCurrentUser(userDetails);
        UserResponseDto user = userService.editUser(currentUser, userProfileEditDto);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/upload-picture")
    public ResponseEntity<Void> updateProfilePicture(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @RequestBody ProfilePictureUploadDto profilePictureUploadDto) {
        User currentUser = userService.getCurrentUser(userDetails);
        userService.updateProfilePicture(currentUser, profilePictureUploadDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove-picture")
    public ResponseEntity<Void> removeProfilePicture(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.getCurrentUser(userDetails);
        userService.removeProfilePicture(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        User currentUser = userService.getCurrentUser(userDetails);
        userService.changePassword(currentUser, passwordChangeDto);
    }

}
