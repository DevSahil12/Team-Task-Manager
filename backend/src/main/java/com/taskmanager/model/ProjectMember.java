package com.taskmanager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectRole role;

    public enum ProjectRole {
        ADMIN, MEMBER
    }

    public ProjectMember() {}

    // ── Builder ────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Project project;
        private User user;
        private ProjectRole role;

        public Builder project(Project project) { this.project = project; return this; }
        public Builder user(User user)          { this.user = user; return this; }
        public Builder role(ProjectRole role)   { this.role = role; return this; }

        public ProjectMember build() {
            ProjectMember pm = new ProjectMember();
            pm.project = this.project;
            pm.user    = this.user;
            pm.role    = this.role;
            return pm;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public Project getProject()                { return project; }
    public void setProject(Project project)    { this.project = project; }
    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }
    public ProjectRole getRole()               { return role; }
    public void setRole(ProjectRole role)      { this.role = role; }
}
