package org.guram.eventscheduler.services;

import org.guram.eventscheduler.DTOs.userDTOs.UserCreateDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserResponseDto;
import org.guram.eventscheduler.DTOs.userDTOs.UserUpdateDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        userRepo.findByEmail(userCreateDto.email()).ifPresent(existing -> {
            throw new ConflictException("A user with email " + userCreateDto.email() + " already exists.");
        });

        String rawPassword = userCreateDto.password();
        String hashed = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setFirstName(userCreateDto.firstName());
        user.setLastName(userCreateDto.lastName());
        user.setEmail(userCreateDto.email());
        user.setPassword(hashed);

        User newUser = userRepo.save(user);
        return Utils.mapUserToResponseDto(newUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userUpdateDto.firstName() != null)
            user.setFirstName(userUpdateDto.firstName());
        if (userUpdateDto.lastName() != null)
            user.setLastName(userUpdateDto.lastName());
        if (userUpdateDto.password() != null) {
            String newPasswordEncoded = passwordEncoder.encode(userUpdateDto.password());
            user.setPassword(newPasswordEncoded);
        }

        User updatedUser = userRepo.save(user);
        return Utils.mapUserToResponseDto(updatedUser);
    }

    public List<UserResponseDto> findAllUsers() {
        return userRepo.findAll().stream()
                .map(Utils::mapUserToResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto findUserById(Long id) {
        User user = findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return Utils.mapUserToResponseDto(user);
    }

    public UserResponseDto findUserByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
        return Utils.mapUserToResponseDto(user);
    }


    private Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }
    private Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}
