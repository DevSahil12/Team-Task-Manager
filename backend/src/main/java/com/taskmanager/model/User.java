package com.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> projectMemberships = new HashSet<>();

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL)
    private Set<Task> assignedTasks = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public User() {}

    // ── Builder ────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String email;
        private String password;

        public Builder name(String name)         { this.name = name; return this; }
        public Builder email(String email)       { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public User build() {
            User u = new User();
            u.name     = this.name;
            u.email    = this.email;
            u.password = this.password;
            return u;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId()                                    { return id; }
    public void setId(Long id)                             { this.id = id; }
    public String getName()                                { return name; }
    public void setName(String name)                       { this.name = name; }
    public String getEmail()                               { return email; }
    public void setEmail(String email)                     { this.email = email; }
    public String getPassword()                            { return password; }
    public void setPassword(String password)               { this.password = password; }
    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public Set<ProjectMember> getProjectMemberships()      { return projectMemberships; }
    public Set<Task> getAssignedTasks()                    { return assignedTasks; }
}
