package org.guram.eventscheduler.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "password", "organizedEvents", "attendances", "sentInvitations", "receivedInvitations" })
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

    @NotBlank
    @Column(nullable = false)
    private String password;

    @ManyToMany(mappedBy = "organizers")
    private Set<Event> organizedEvents = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Attendance> attendances = new HashSet<>();

    @OneToMany(mappedBy = "invitee")
    private Set<Invitation> receivedInvitations = new HashSet<>();

    @OneToMany(mappedBy = "invitor")
    private Set<Invitation> sentInvitations = new HashSet<>();

}
