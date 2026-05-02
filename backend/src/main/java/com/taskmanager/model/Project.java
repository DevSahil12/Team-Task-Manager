package com.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Project() {}

    // ── Builder ────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String description;
        private User createdBy;

        public Builder name(String name)               { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder createdBy(User createdBy)       { this.createdBy = createdBy; return this; }

        public Project build() {
            Project p = new Project();
            p.name        = this.name;
            p.description = this.description;
            p.createdBy   = this.createdBy;
            return p;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getDescription()             { return description; }
    public void setDescription(String desc)    { this.description = desc; }
    public User getCreatedBy()                 { return createdBy; }
    public void setCreatedBy(User createdBy)   { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public Set<ProjectMember> getMembers()     { return members; }
    public List<Task> getTasks()               { return tasks; }
}
