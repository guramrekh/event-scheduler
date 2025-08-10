package org.guram.eventscheduler.services;

import org.guram.eventscheduler.cloudinary.CloudinaryService;
import org.guram.eventscheduler.dtos.userDtos.PasswordChangeDto;
import org.guram.eventscheduler.dtos.userDtos.ProfilePictureUploadDto;
import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserProfileEditDto;
import org.guram.eventscheduler.dtos.userDtos.UserResponseDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.exceptions.UserNotFoundException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.UserRepository;
import org.guram.eventscheduler.utils.EntityToDtoMappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import static org.guram.eventscheduler.utils.EntityToDtoMappings.mapUserToResponseDto;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
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
        return mapUserToResponseDto(newUser);
    }

    @Transactional
    public void deleteUser(User currentUser) {
        userRepo.deleteById(currentUser.getId());
    }

    @Transactional
    public UserResponseDto editUser(User user, UserProfileEditDto userProfileEditDto) {
        user.setFirstName(userProfileEditDto.firstName());
        user.setLastName(userProfileEditDto.lastName());
        user.setBio(userProfileEditDto.bio());

        User updatedUser = userRepo.save(user);
        return mapUserToResponseDto(updatedUser);
    }

    @Transactional
    public void updateProfilePicture(User user, ProfilePictureUploadDto profilePictureUploadDto) {
        user.setProfilePictureUrl(profilePictureUploadDto.imageUrl());
        userRepo.save(user);
    }

    @Transactional
    public void removeProfilePicture(User user) {
        String existingImageUrl = user.getProfilePictureUrl();

        user.setProfilePictureUrl(null);
        userRepo.save(user);

        if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
            cloudinaryService.deleteImage(existingImageUrl);
        }
    }

    @Transactional
    public void changePassword(User user, PasswordChangeDto passwordChangeDto) {
        if (!passwordEncoder.matches(passwordChangeDto.currentPassword(), user.getPassword()))
            throw new ConflictException("Incorrect current password.");

        if (passwordEncoder.matches(passwordChangeDto.newPassword(), user.getPassword()))
            throw new ConflictException("New password cannot be same as the old password");

        String hashedPassword = passwordEncoder.encode(passwordChangeDto.newPassword());
        user.setPassword(hashedPassword);
        userRepo.save(user);
    }

    public UserResponseDto findUserByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
        return mapUserToResponseDto(user);
    }

    public User getCurrentUser(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email + ". This should not happen for an authenticated user."));
    }

    public List<UserResponseDto> findUsersByName(String firstName, String lastName) {
        return userRepo.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc(firstName, lastName).stream()
                .map(EntityToDtoMappings::mapUserToResponseDto)
                .toList();
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}
