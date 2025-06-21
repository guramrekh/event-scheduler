package org.guram.eventscheduler.services;

import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.dtos.userDtos.UserEditDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
    public void deleteUser(User currentUser) {
        userRepo.deleteById(currentUser.getId());
    }

    @Transactional
    public UserResponseDto editUser(User user, UserEditDto userEditDto) {
        if (userEditDto.firstName() != null)
            user.setFirstName(userEditDto.firstName());
        if (userEditDto.lastName() != null)
            user.setLastName(userEditDto.lastName());
        if (userEditDto.password() != null) {
            String newPasswordEncoded = passwordEncoder.encode(userEditDto.password());
            user.setPassword(newPasswordEncoded);
        }

        User updatedUser = userRepo.save(user);
        return Utils.mapUserToResponseDto(updatedUser);
    }

    public UserResponseDto findUserByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
        return Utils.mapUserToResponseDto(user);
    }

    public User getCurrentUser(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email + ". This should not happen for an authenticated user."));
    }

    public List<UserResponseDto> findUsersByName(String firstName, String lastName) {
        return userRepo.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc(firstName, lastName).stream()
                .map(Utils::mapUserToResponseDto)
                .toList();
    }

    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}
