package org.guram.eventscheduler.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "attendances", "invitations" })
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank()
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @NotNull()
    @Future()
    @Column(nullable = false)
    private LocalDateTime dateTime;

    @NotBlank()
    @Size(min = 3, max = 100)
    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private boolean isCancelled = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Attendance> attendances = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("invitationSentDate DESC")
    private List<Invitation> invitations = new ArrayList<>();

}
