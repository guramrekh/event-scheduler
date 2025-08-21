package org.guram.eventscheduler.repositories;

import org.guram.eventscheduler.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = new User("john", "cena", "john.cena@email.com", "<PASSWORD>");
        User user2 = new User("john", "wick", "john.wick@email.com", "<PASSWORD>");
        User user3 = new User("john", "wick", "wick.john@email.com", "<PASSWORD>");
        User user4 = new User("joh", "wick", "wick.joh@email.com", "<PASSWORD>");
        User user5 = new User("jamal", "wick", "wick.jamal@email.com", "<PASSWORD>");

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
    }


    @Test
    void findByEmail_shouldReturnUser_whenEmailExistsInDb() {
        Optional<User> optionalUserByEmail = userRepository.findByEmail("john.cena@email.com");

        assertThat(optionalUserByEmail)
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("john.cena@email.com");
    }


    @Test
    void findByEmail_shouldReturnEmptyOptional_whenEmailNotExistsInDb() {
        Optional<User> optionalUserByEmail = userRepository.findByEmail("non-existent@email.com");

        assertThat(optionalUserByEmail).isEmpty();
    }

    @Test
    void findByNamesIgnoreCaseOrderByEmailAsc_shouldReturnCorrectUsersOrdered_whenBothNamesAndCaseMatch() {
        List<User> foundUsers = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc("john", "wick");

        assertThat(foundUsers)
                .extracting(User::getEmail)
                .containsExactly("john.wick@email.com", "wick.john@email.com");
    }

    @Test
    void findByNamesIgnoreCaseOrderByEmailAsc_shouldReturnCorrectUsersOrdered_whenBothNamesButNotCaseMatch() {
        List<User> foundUsers = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc("jOHn", "WiCk");

        assertThat(foundUsers)
                .extracting(User::getEmail)
                .containsExactly("john.wick@email.com", "wick.john@email.com");
    }

    @Test
    void findByNamesIgnoreCaseOrderByEmailAsc_shouldReturnEmptyList_whenNoNamesMatch() {
        List<User> foundUsers = userRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCaseOrderByEmailAsc("jamal", "cena");

        assertThat(foundUsers).isEmpty();
    }

}
