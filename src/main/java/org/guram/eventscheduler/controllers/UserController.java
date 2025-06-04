package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserUpdateDto;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static org.guram.eventscheduler.controllers.Utils.getCurrentUser;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto user) {
        UserResponseDto newUser = userService.createUser(user);
        URI location = URI.create("/user/" + newUser.id());
        return ResponseEntity.created(location).body(newUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        UserResponseDto user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        UserResponseDto user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDto>> listAllUsers() {
        List<UserResponseDto> all = userService.findAllUsers();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getCurrentUser(userDetails, userService).getId();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/edit")
    public ResponseEntity<UserResponseDto> updateUser(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody UserUpdateDto userUpdateDto) {
        Long userId = getCurrentUser(userDetails, userService).getId();
        UserResponseDto user = userService.updateUser(userId, userUpdateDto);
        return ResponseEntity.ok(user);
    }

}
