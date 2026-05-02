package com.taskmanager.dto;

import com.taskmanager.model.ProjectMember;
import com.taskmanager.model.Task;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Dtos {

    // ═══════════════════════════════════════════════════════════
    // AUTH
    // ═══════════════════════════════════════════════════════════

    public static class RegisterRequest {
        @NotBlank private String name;
        @Email @NotBlank private String email;
        @NotBlank private String password;

        public RegisterRequest() {}
        public String getName()        { return name; }
        public void setName(String n)  { this.name = n; }
        public String getEmail()       { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getPassword()    { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;

        public LoginRequest() {}
        public String getEmail()          { return email; }
        public void setEmail(String e)    { this.email = e; }
        public String getPassword()       { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class AuthResponse {
        private String token;
        private Long userId;
        private String name;
        private String email;

        public AuthResponse() {}
        private AuthResponse(Builder b) {
            this.token = b.token; this.userId = b.userId;
            this.name = b.name; this.email = b.email;
        }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private String token; private Long userId; private String name; private String email;
            public Builder token(String token)   { this.token = token; return this; }
            public Builder userId(Long userId)   { this.userId = userId; return this; }
            public Builder name(String name)     { this.name = name; return this; }
            public Builder email(String email)   { this.email = email; return this; }
            public AuthResponse build()          { return new AuthResponse(this); }
        }
        public String getToken()  { return token; }
        public Long getUserId()   { return userId; }
        public String getName()   { return name; }
        public String getEmail()  { return email; }
    }

    // ═══════════════════════════════════════════════════════════
    // USER
    // ═══════════════════════════════════════════════════════════

    public static class UserSummary {
        private Long id; private String name; private String email;

        public UserSummary() {}
        private UserSummary(Builder b) { this.id = b.id; this.name = b.name; this.email = b.email; }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Long id; private String name; private String email;
            public Builder id(Long id)         { this.id = id; return this; }
            public Builder name(String name)   { this.name = name; return this; }
            public Builder email(String email) { this.email = email; return this; }
            public UserSummary build()         { return new UserSummary(this); }
        }
        public Long getId()    { return id; }
        public String getName()  { return name; }
        public String getEmail() { return email; }
    }

    // ═══════════════════════════════════════════════════════════
    // PROJECT
    // ═══════════════════════════════════════════════════════════

    public static class CreateProjectRequest {
        @NotBlank private String name;
        private String description;

        public CreateProjectRequest() {}
        public String getName()             { return name; }
        public void setName(String name)    { this.name = name; }
        public String getDescription()      { return description; }
        public void setDescription(String d){ this.description = d; }
    }

    public static class ProjectResponse {
        private Long id; private String name; private String description;
        private UserSummary createdBy; private LocalDateTime createdAt;
        private int memberCount; private int taskCount;
        private ProjectMember.ProjectRole myRole;

        public ProjectResponse() {}
        private ProjectResponse(Builder b) {
            this.id = b.id; this.name = b.name; this.description = b.description;
            this.createdBy = b.createdBy; this.createdAt = b.createdAt;
            this.memberCount = b.memberCount; this.taskCount = b.taskCount;
            this.myRole = b.myRole;
        }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Long id; private String name; private String description;
            private UserSummary createdBy; private LocalDateTime createdAt;
            private int memberCount; private int taskCount;
            private ProjectMember.ProjectRole myRole;
            public Builder id(Long id)                                      { this.id = id; return this; }
            public Builder name(String name)                                { this.name = name; return this; }
            public Builder description(String description)                  { this.description = description; return this; }
            public Builder createdBy(UserSummary createdBy)                 { this.createdBy = createdBy; return this; }
            public Builder createdAt(LocalDateTime createdAt)               { this.createdAt = createdAt; return this; }
            public Builder memberCount(int memberCount)                     { this.memberCount = memberCount; return this; }
            public Builder taskCount(int taskCount)                         { this.taskCount = taskCount; return this; }
            public Builder myRole(ProjectMember.ProjectRole myRole)         { this.myRole = myRole; return this; }
            public ProjectResponse build()                                  { return new ProjectResponse(this); }
        }
        public Long getId()                          { return id; }
        public String getName()                      { return name; }
        public String getDescription()               { return description; }
        public UserSummary getCreatedBy()            { return createdBy; }
        public LocalDateTime getCreatedAt()          { return createdAt; }
        public int getMemberCount()                  { return memberCount; }
        public int getTaskCount()                    { return taskCount; }
        public ProjectMember.ProjectRole getMyRole() { return myRole; }
    }

    public static class AddMemberRequest {
        @NotNull private Long userId;
        private ProjectMember.ProjectRole role = ProjectMember.ProjectRole.MEMBER;

        public AddMemberRequest() {}
        public Long getUserId()                         { return userId; }
        public void setUserId(Long userId)              { this.userId = userId; }
        public ProjectMember.ProjectRole getRole()      { return role; }
        public void setRole(ProjectMember.ProjectRole r){ this.role = r; }
    }

    public static class MemberResponse {
        private Long userId; private String name; private String email;
        private ProjectMember.ProjectRole role;

        public MemberResponse() {}
        private MemberResponse(Builder b) {
            this.userId = b.userId; this.name = b.name;
            this.email = b.email; this.role = b.role;
        }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Long userId; private String name; private String email;
            private ProjectMember.ProjectRole role;
            public Builder userId(Long userId)                      { this.userId = userId; return this; }
            public Builder name(String name)                        { this.name = name; return this; }
            public Builder email(String email)                      { this.email = email; return this; }
            public Builder role(ProjectMember.ProjectRole role)     { this.role = role; return this; }
            public MemberResponse build()                           { return new MemberResponse(this); }
        }
        public Long getUserId()                      { return userId; }
        public String getName()                      { return name; }
        public String getEmail()                     { return email; }
        public ProjectMember.ProjectRole getRole()   { return role; }
    }

    // ═══════════════════════════════════════════════════════════
    // TASK
    // ═══════════════════════════════════════════════════════════

    public static class CreateTaskRequest {
        @NotBlank private String title;
        private String description;
        private LocalDate dueDate;
        private Task.Priority priority;
        private Long assignedToId;
        @NotNull private Long projectId;

        public CreateTaskRequest() {}
        public String getTitle()                  { return title; }
        public void setTitle(String title)        { this.title = title; }
        public String getDescription()            { return description; }
        public void setDescription(String d)      { this.description = d; }
        public LocalDate getDueDate()             { return dueDate; }
        public void setDueDate(LocalDate d)       { this.dueDate = d; }
        public Task.Priority getPriority()        { return priority; }
        public void setPriority(Task.Priority p)  { this.priority = p; }
        public Long getAssignedToId()             { return assignedToId; }
        public void setAssignedToId(Long id)      { this.assignedToId = id; }
        public Long getProjectId()                { return projectId; }
        public void setProjectId(Long id)         { this.projectId = id; }
    }

    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private LocalDate dueDate;
        private Task.Priority priority;
        private Task.TaskStatus status;
        private Long assignedToId;

        public UpdateTaskRequest() {}
        public String getTitle()                  { return title; }
        public void setTitle(String title)        { this.title = title; }
        public String getDescription()            { return description; }
        public void setDescription(String d)      { this.description = d; }
        public LocalDate getDueDate()             { return dueDate; }
        public void setDueDate(LocalDate d)       { this.dueDate = d; }
        public Task.Priority getPriority()        { return priority; }
        public void setPriority(Task.Priority p)  { this.priority = p; }
        public Task.TaskStatus getStatus()        { return status; }
        public void setStatus(Task.TaskStatus s)  { this.status = s; }
        public Long getAssignedToId()             { return assignedToId; }
        public void setAssignedToId(Long id)      { this.assignedToId = id; }
    }

    public static class TaskResponse {
        private Long id; private String title; private String description;
        private Task.TaskStatus status; private Task.Priority priority;
        private LocalDate dueDate; private LocalDateTime createdAt;
        private UserSummary assignedTo; private UserSummary createdBy;
        private Long projectId; private String projectName; private boolean overdue;

        public TaskResponse() {}
        private TaskResponse(Builder b) {
            this.id = b.id; this.title = b.title; this.description = b.description;
            this.status = b.status; this.priority = b.priority; this.dueDate = b.dueDate;
            this.createdAt = b.createdAt; this.assignedTo = b.assignedTo;
            this.createdBy = b.createdBy; this.projectId = b.projectId;
            this.projectName = b.projectName; this.overdue = b.overdue;
        }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private Long id; private String title; private String description;
            private Task.TaskStatus status; private Task.Priority priority;
            private LocalDate dueDate; private LocalDateTime createdAt;
            private UserSummary assignedTo; private UserSummary createdBy;
            private Long projectId; private String projectName; private boolean overdue;

            public Builder id(Long id)                      { this.id = id; return this; }
            public Builder title(String title)              { this.title = title; return this; }
            public Builder description(String description)  { this.description = description; return this; }
            public Builder status(Task.TaskStatus status)   { this.status = status; return this; }
            public Builder priority(Task.Priority priority) { this.priority = priority; return this; }
            public Builder dueDate(LocalDate dueDate)       { this.dueDate = dueDate; return this; }
            public Builder createdAt(LocalDateTime createdAt){ this.createdAt = createdAt; return this; }
            public Builder assignedTo(UserSummary assignedTo){ this.assignedTo = assignedTo; return this; }
            public Builder createdBy(UserSummary createdBy) { this.createdBy = createdBy; return this; }
            public Builder projectId(Long projectId)        { this.projectId = projectId; return this; }
            public Builder projectName(String projectName)  { this.projectName = projectName; return this; }
            public Builder overdue(boolean overdue)         { this.overdue = overdue; return this; }
            public TaskResponse build()                     { return new TaskResponse(this); }
        }
        public Long getId()                   { return id; }
        public String getTitle()              { return title; }
        public String getDescription()        { return description; }
        public Task.TaskStatus getStatus()    { return status; }
        public Task.Priority getPriority()    { return priority; }
        public LocalDate getDueDate()         { return dueDate; }
        public LocalDateTime getCreatedAt()   { return createdAt; }
        public UserSummary getAssignedTo()    { return assignedTo; }
        public UserSummary getCreatedBy()     { return createdBy; }
        public Long getProjectId()            { return projectId; }
        public String getProjectName()        { return projectName; }
        public boolean isOverdue()            { return overdue; }
    }

    // ═══════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════

    public static class DashboardStats {
        private long totalTasks; private long todoCount;
        private long inProgressCount; private long doneCount;
        private long overdueCount; private int projectCount;

        public DashboardStats() {}
        private DashboardStats(Builder b) {
            this.totalTasks = b.totalTasks; this.todoCount = b.todoCount;
            this.inProgressCount = b.inProgressCount; this.doneCount = b.doneCount;
            this.overdueCount = b.overdueCount; this.projectCount = b.projectCount;
        }
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private long totalTasks; private long todoCount;
            private long inProgressCount; private long doneCount;
            private long overdueCount; private int projectCount;
            public Builder totalTasks(long totalTasks)         { this.totalTasks = totalTasks; return this; }
            public Builder todoCount(long todoCount)           { this.todoCount = todoCount; return this; }
            public Builder inProgressCount(long c)             { this.inProgressCount = c; return this; }
            public Builder doneCount(long doneCount)           { this.doneCount = doneCount; return this; }
            public Builder overdueCount(long overdueCount)     { this.overdueCount = overdueCount; return this; }
            public Builder projectCount(int projectCount)      { this.projectCount = projectCount; return this; }
            public DashboardStats build()                      { return new DashboardStats(this); }
        }
        public long getTotalTasks()      { return totalTasks; }
        public long getTodoCount()       { return todoCount; }
        public long getInProgressCount() { return inProgressCount; }
        public long getDoneCount()       { return doneCount; }
        public long getOverdueCount()    { return overdueCount; }
        public int getProjectCount()     { return projectCount; }
    }
}
