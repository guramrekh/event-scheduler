package org.guram.eventscheduler.services;

import org.guram.eventscheduler.models.User;
import org.guram.eventscheduler.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public User createUser(User user) {
        userRepo.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("A user with email " + user.getEmail() + " already exists.");
        });

        String rawPassword = user.getPassword();
        String hashed = passwordEncoder.encode(rawPassword);
        user.setPassword(hashed);

        return userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public List<User> findAllUsers() {
        return userRepo.findAll();
    }
}
