package com.taskmanager.controller;

import com.taskmanager.dto.Dtos;
import com.taskmanager.model.Project;
import com.taskmanager.model.ProjectMember;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, ProjectRepository projectRepository,
                          ProjectMemberRepository memberRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    private Dtos.TaskResponse toResponse(Task t) {
        boolean overdue = t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDate.now())
                && t.getStatus() != Task.TaskStatus.DONE;

        Dtos.UserSummary assignedTo = t.getAssignedTo() == null ? null :
                Dtos.UserSummary.builder()
                        .id(t.getAssignedTo().getId())
                        .name(t.getAssignedTo().getName())
                        .email(t.getAssignedTo().getEmail()).build();

        Dtos.UserSummary createdBy = Dtos.UserSummary.builder()
                .id(t.getCreatedBy().getId())
                .name(t.getCreatedBy().getName())
                .email(t.getCreatedBy().getEmail()).build();

        return Dtos.TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .dueDate(t.getDueDate())
                .createdAt(t.getCreatedAt())
                .assignedTo(assignedTo)
                .createdBy(createdBy)
                .projectId(t.getProject().getId())
                .projectName(t.getProject().getName())
                .overdue(overdue)
                .build();
    }

    /** Get tasks assigned to current user */
    @GetMapping("/my")
    public ResponseEntity<List<Dtos.TaskResponse>> getMyTasks(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        List<Dtos.TaskResponse> tasks = taskRepository.findByAssignedToId(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    /** Get all tasks in a project (must be a member) */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getByProject(@PathVariable Long projectId,
                                           @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        if (!memberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        List<Dtos.TaskResponse> tasks = taskRepository.findByProjectId(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(tasks);
    }

    /** Get a single task */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();
        if (!memberRepository.existsByProjectIdAndUserId(task.getProject().getId(), user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(toResponse(task));
    }

    /** Create a task — Admin only */
    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@Valid @RequestBody Dtos.CreateTaskRequest req,
                                    @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        ProjectMember membership = memberRepository
                .findByProjectIdAndUserId(req.getProjectId(), user.getId()).orElse(null);
        if (membership == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Not a project member"));
        }
        if (membership.getRole() != ProjectMember.ProjectRole.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can create tasks"));
        }

        Project project = projectRepository.findById(req.getProjectId()).orElseThrow();
        User assignedTo = null;
        if (req.getAssignedToId() != null) {
            assignedTo = userRepository.findById(req.getAssignedToId()).orElse(null);
            if (assignedTo != null && !memberRepository.existsByProjectIdAndUserId(project.getId(), assignedTo.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Assigned user is not a project member"));
            }
        }

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .priority(req.getPriority() != null ? req.getPriority() : Task.Priority.MEDIUM)
                .status(Task.TaskStatus.TODO)
                .project(project)
                .assignedTo(assignedTo)
                .createdBy(user)
                .build();
        taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(task));
    }

    /** Update a task — Admin can update all fields; Member can only update status of assigned tasks */
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Dtos.UpdateTaskRequest req,
                                    @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();

        ProjectMember membership = memberRepository
                .findByProjectIdAndUserId(task.getProject().getId(), user.getId()).orElse(null);
        if (membership == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }

        boolean isAdmin = membership.getRole() == ProjectMember.ProjectRole.ADMIN;
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(user.getId());

        if (!isAdmin && !isAssignee) {
            return ResponseEntity.status(403).body(Map.of("error", "You can only update tasks assigned to you"));
        }

        if (isAdmin) {
            // Admin can update everything
            if (req.getTitle() != null) task.setTitle(req.getTitle());
            if (req.getDescription() != null) task.setDescription(req.getDescription());
            if (req.getDueDate() != null) task.setDueDate(req.getDueDate());
            if (req.getPriority() != null) task.setPriority(req.getPriority());
            if (req.getAssignedToId() != null) {
                User assignedTo = userRepository.findById(req.getAssignedToId()).orElse(null);
                if (assignedTo != null && memberRepository.existsByProjectIdAndUserId(task.getProject().getId(), assignedTo.getId())) {
                    task.setAssignedTo(assignedTo);
                }
            }
        }

        // Both admin and assignee can update status
        if (req.getStatus() != null) task.setStatus(req.getStatus());

        taskRepository.save(task);
        return ResponseEntity.ok(toResponse(task));
    }

    /** Delete a task — Admin only */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        Task task = taskRepository.findById(id).orElse(null);
        if (task == null) return ResponseEntity.notFound().build();

        ProjectMember membership = memberRepository
                .findByProjectIdAndUserId(task.getProject().getId(), user.getId()).orElse(null);
        if (membership == null || membership.getRole() != ProjectMember.ProjectRole.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can delete tasks"));
        }

        taskRepository.delete(task);
        return ResponseEntity.noContent().build();
    }
}
