package org.guram.eventscheduler.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "profilePictureUrl", "password", "attendances", "sentInvitations", "receivedInvitations" })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank()
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String lastName;

    @Email
    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    private String profilePictureUrl;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Attendance> attendances = new ArrayList<>();

    @OneToMany(mappedBy = "invitee")
    @OrderBy("invitationSentDate DESC")
    private List<Invitation> receivedInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "invitor")
    @OrderBy("invitationSentDate DESC")
    private List<Invitation> sentInvitations = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Notification> notifications = new ArrayList<>();

}
