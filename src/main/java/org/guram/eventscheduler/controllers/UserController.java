package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserEditDto;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.services.UserService;
import org.guram.eventscheduler.services.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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
        UserResponseDto user = Utils.mapUserToResponseDto(userService.getCurrentUser(userDetails));
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
                                                      @Valid @RequestBody UserEditDto userEditDto) {
        User currentUser = userService.getCurrentUser(userDetails);
        UserResponseDto user = userService.editUser(currentUser, userEditDto);
        return ResponseEntity.ok(user);
    }

}
