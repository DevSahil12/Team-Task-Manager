package com.taskmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null)   status   = TaskStatus.TODO;
        if (priority == null) priority = Priority.MEDIUM;
    }

    public enum TaskStatus { TODO, IN_PROGRESS, DONE }
    public enum Priority   { LOW, MEDIUM, HIGH }

    public Task() {}

    // ── Builder ────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String title;
        private String description;
        private TaskStatus status;
        private Priority priority;
        private LocalDate dueDate;
        private Project project;
        private User assignedTo;
        private User createdBy;

        public Builder title(String title)             { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(TaskStatus status)       { this.status = status; return this; }
        public Builder priority(Priority priority)     { this.priority = priority; return this; }
        public Builder dueDate(LocalDate dueDate)      { this.dueDate = dueDate; return this; }
        public Builder project(Project project)        { this.project = project; return this; }
        public Builder assignedTo(User assignedTo)     { this.assignedTo = assignedTo; return this; }
        public Builder createdBy(User createdBy)       { this.createdBy = createdBy; return this; }

        public Task build() {
            Task t = new Task();
            t.title       = this.title;
            t.description = this.description;
            t.status      = this.status;
            t.priority    = this.priority;
            t.dueDate     = this.dueDate;
            t.project     = this.project;
            t.assignedTo  = this.assignedTo;
            t.createdBy   = this.createdBy;
            return t;
        }
    }

    // ── Getters & Setters ──────────────────────────────────────
    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }
    public String getDescription()               { return description; }
    public void setDescription(String desc)      { this.description = desc; }
    public TaskStatus getStatus()                { return status; }
    public void setStatus(TaskStatus status)     { this.status = status; }
    public Priority getPriority()                { return priority; }
    public void setPriority(Priority priority)   { this.priority = priority; }
    public LocalDate getDueDate()                { return dueDate; }
    public void setDueDate(LocalDate dueDate)    { this.dueDate = dueDate; }
    public Project getProject()                  { return project; }
    public void setProject(Project project)      { this.project = project; }
    public User getAssignedTo()                  { return assignedTo; }
    public void setAssignedTo(User assignedTo)   { this.assignedTo = assignedTo; }
    public User getCreatedBy()                   { return createdBy; }
    public void setCreatedBy(User createdBy)     { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
}
