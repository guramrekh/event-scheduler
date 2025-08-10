package org.guram.eventscheduler.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = { "profilePictureUrl", "password", "attendances", "sentInvitations", "receivedInvitations" })
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Exclude
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

    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user")
    private List<Attendance> attendances = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "invitee")
    @OrderBy("invitationSentDate DESC")
    private List<Invitation> receivedInvitations = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "invitor")
    @OrderBy("invitationSentDate DESC")
    private List<Invitation> sentInvitations = new ArrayList<>();

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Notification> notifications = new ArrayList<>();


    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

}
