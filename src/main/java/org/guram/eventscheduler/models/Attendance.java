package org.guram.eventscheduler.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "event_id" }))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.REGISTERED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceRole role;


    public Attendance(User user, Event event, AttendanceRole role) {
        this.user = user;
        this.event = event;
        this.role = role;
    }
}
