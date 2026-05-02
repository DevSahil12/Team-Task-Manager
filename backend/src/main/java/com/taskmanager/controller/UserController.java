package com.taskmanager.controller;

import com.taskmanager.dto.Dtos;
import com.taskmanager.model.User;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Search user by email — used by Add Member modal */
    @GetMapping("/search")
    public ResponseEntity<?> searchByEmail(@RequestParam String email,
                                           @AuthenticationPrincipal UserDetails principal) {
        Optional<User> found = userRepository.findByEmail(email.trim());
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No user found with that email"));
        }
        User u = found.get();
        // Don't reveal your own profile through search
        return ResponseEntity.ok(
                Dtos.UserSummary.builder().id(u.getId()).name(u.getName()).email(u.getEmail()).build()
        );
    }

    /** Get current logged-in user profile */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .map(u -> ResponseEntity.ok(
                        Dtos.UserSummary.builder().id(u.getId()).name(u.getName()).email(u.getEmail()).build()))
                .orElse(ResponseEntity.notFound().build());
    }
}
