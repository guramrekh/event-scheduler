package org.guram.eventscheduler.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attendances",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "event_id" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status = AttendanceStatus.REGISTERED;
}
