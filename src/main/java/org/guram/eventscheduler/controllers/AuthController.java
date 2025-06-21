package org.guram.eventscheduler.controllers;

import jakarta.validation.Valid;
import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserCreateDto user) {
        UserResponseDto newUser = userService.createUser(user);
        URI location = URI.create("/users/" + newUser.id());
        return ResponseEntity.created(location).body(newUser);
    }
}
