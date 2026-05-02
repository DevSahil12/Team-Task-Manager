package com.taskmanager.controller;

import com.taskmanager.dto.Dtos;
import com.taskmanager.model.ProjectMember;
import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectMemberRepository;
import com.taskmanager.repository.ProjectRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;

    public DashboardController(TaskRepository taskRepository, ProjectRepository projectRepository,
                                ProjectMemberRepository memberRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    @GetMapping
    public ResponseEntity<?> getStats(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        LocalDate today = LocalDate.now();

        List<Task> myTasks = taskRepository.findByAssignedToId(user.getId());
        long total = myTasks.size();
        long inProgress = myTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
        long done = myTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
        long todo = myTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.TODO).count();
        long overdue = myTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && t.getStatus() != Task.TaskStatus.DONE)
                .count();

        int projectCount = projectRepository.findAllByMemberUserId(user.getId()).size();

        // Tasks per user: aggregate across all projects the current user is ADMIN of
        List<ProjectMember> adminMemberships = memberRepository.findByProjectId(0L); // placeholder
        // Get all projects where user is admin
        List<Long> adminProjectIds = memberRepository
                .findAll().stream()
                .filter(pm -> pm.getUser().getId().equals(user.getId())
                        && pm.getRole() == ProjectMember.ProjectRole.ADMIN)
                .map(pm -> pm.getProject().getId())
                .collect(Collectors.toList());

        List<Map<String, Object>> tasksPerUser = new ArrayList<>();
        if (!adminProjectIds.isEmpty()) {
            // Get all tasks in admin projects
            Map<String, Long> countByUser = new LinkedHashMap<>();
            for (Long pid : adminProjectIds) {
                taskRepository.findByProjectId(pid).forEach(t -> {
                    if (t.getAssignedTo() != null) {
                        String name = t.getAssignedTo().getName();
                        countByUser.merge(name, 1L, Long::sum);
                    }
                });
            }
            countByUser.forEach((name, count) -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", name);
                entry.put("count", count);
                tasksPerUser.add(entry);
            });
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTasks", total);
        result.put("todoCount", todo);
        result.put("inProgressCount", inProgress);
        result.put("doneCount", done);
        result.put("overdueCount", overdue);
        result.put("projectCount", projectCount);
        result.put("tasksPerUser", tasksPerUser);

        return ResponseEntity.ok(result);
    }
}
