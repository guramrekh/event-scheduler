package org.guram.eventscheduler.models;

import jakarta.persistence.*;
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

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

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
