package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    List<User> findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc(String firstName, String lastName);
}
