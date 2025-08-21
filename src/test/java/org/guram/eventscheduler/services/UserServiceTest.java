package org.guram.eventscheduler.services;

import org.guram.eventscheduler.cloudinary.CloudinaryService;
import org.guram.eventscheduler.dtos.userDtos.PasswordChangeDto;
import org.guram.eventscheduler.dtos.userDtos.ProfilePictureUploadDto;
import org.guram.eventscheduler.dtos.userDtos.UserCreateDto;
import org.guram.eventscheduler.dtos.userDtos.UserProfileEditDto;
import org.guram.eventscheduler.exceptions.ConflictException;
import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private UserService userService;


    @Test
    void createUser_shouldSucceed_whenEmailIsUnique() {
        var userCreateDto = new UserCreateDto(
                "john", "wick", "john.wick@email.com", "rawPassword");

        when(userRepo.findByEmail("john.wick@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(userCreateDto);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getFirstName()).isEqualTo("john");
        assertThat(savedUser.getLastName()).isEqualTo("wick");
        assertThat(savedUser.getEmail()).isEqualTo("john.wick@email.com");
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    void createUser_shouldThrowException_whenEmailExists() {
        var userCreateDto = new UserCreateDto(
                "john", "wick", "john.wick@email.com", "rawPassword");
        User existingUser = new User();

        when(userRepo.findByEmail("john.wick@email.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ConflictException.class, () -> userService.createUser(userCreateDto));

        verify(userRepo, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void editUser_happyPath() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var userEditDto = new UserProfileEditDto("newFirstName", "newLastName", "newBio");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.editUser(user, userEditDto);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getFirstName()).isEqualTo("newFirstName");
        assertThat(savedUser.getLastName()).isEqualTo("newLastName");
        assertThat(savedUser.getBio()).isEqualTo("newBio");
    }

    @Test
    void updateProfilePicture_happyPath() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var dto = new ProfilePictureUploadDto("imageUrl");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updateProfilePicture(user, dto);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getProfilePictureUrl()).isEqualTo("imageUrl");
    }

    @Test
    void removeProfilePicture_shouldSucceed_whenProfilePictureExist() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        user.setProfilePictureUrl("imageUrl");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.removeProfilePicture(user);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepo).save(userArgumentCaptor.capture());
        verify(cloudinaryService).deleteImage(stringArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        String removedImageUrl = stringArgumentCaptor.getValue();

        assertThat(savedUser.getProfilePictureUrl()).isNull();
        assertThat(removedImageUrl).isEqualTo("imageUrl");
    }

    @Test
    void removeProfilePicture_shouldDoNothing_whenProfilePictureNotExists() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.removeProfilePicture(user);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(userArgumentCaptor.capture());
        verify(cloudinaryService, never()).deleteImage(anyString());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getProfilePictureUrl()).isNull();
    }

    @Test
    void changePassword_shouldSucceed_whenValidInput() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var dto = new PasswordChangeDto("<PASSWORD>", "newPassword", "newPassword");

        when(passwordEncoder.matches("<PASSWORD>", "<PASSWORD>")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "<PASSWORD>")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedNewPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(user, dto);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepo).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getPassword()).isEqualTo("hashedNewPassword");
    }

    @Test
    void changePassword_shouldThrowException_whenIncorrectCurrentPassword() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var dto = new PasswordChangeDto("wrongPassword", "newPassword", "newPassword");

        when(passwordEncoder.matches("wrongPassword", "<PASSWORD>")).thenReturn(false);

        assertThrows(ConflictException.class, () -> userService.changePassword(user, dto));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordIsSame() {
        User user = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        var dto = new PasswordChangeDto("<PASSWORD>", "<PASSWORD>", "<PASSWORD>");

        when(passwordEncoder.matches("<PASSWORD>", "<PASSWORD>"))
                .thenReturn(true)   // for the first call to pass
                .thenReturn(true);  // for the second call to fail

        assertThrows(ConflictException.class, () -> userService.changePassword(user, dto));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(User.class));
    }

}
