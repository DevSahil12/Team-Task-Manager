package com.taskmanager.controller;

import com.taskmanager.dto.Dtos;
import com.taskmanager.model.Project;
import com.taskmanager.model.ProjectMember;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ProjectController(ProjectRepository projectRepository,
                             ProjectMemberRepository memberRepository,
                             UserRepository userRepository,
                             TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    private Dtos.ProjectResponse toResponse(Project p, Long currentUserId) {
        ProjectMember.ProjectRole myRole = memberRepository
                .findByProjectIdAndUserId(p.getId(), currentUserId)
                .map(ProjectMember::getRole)
                .orElse(ProjectMember.ProjectRole.MEMBER);

        return Dtos.ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .createdBy(Dtos.UserSummary.builder()
                        .id(p.getCreatedBy().getId())
                        .name(p.getCreatedBy().getName())
                        .email(p.getCreatedBy().getEmail())
                        .build())
                .createdAt(p.getCreatedAt())
                .memberCount(memberRepository.findByProjectId(p.getId()).size())
                .taskCount(taskRepository.findByProjectId(p.getId()).size())
                .myRole(myRole)
                .build();
    }

    @GetMapping
    public ResponseEntity<List<Dtos.ProjectResponse>> getAll(@AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        List<Project> projects = projectRepository.findAllByMemberUserId(user.getId());
        return ResponseEntity.ok(projects.stream()
                .map(p -> toResponse(p, user.getId()))
                .collect(Collectors.toList()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Dtos.ProjectResponse> create(@Valid @RequestBody Dtos.CreateProjectRequest req,
                                                        @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .createdBy(user)
                .build();
        projectRepository.save(project);

        // Creator is automatically ADMIN
        ProjectMember membership = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(ProjectMember.ProjectRole.ADMIN)
                .build();
        memberRepository.save(membership);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(project, user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        return projectRepository.findById(id)
                .filter(p -> memberRepository.existsByProjectIdAndUserId(p.getId(), user.getId()))
                .map(p -> ResponseEntity.ok(toResponse(p, user.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        if (!memberRepository.existsByProjectIdAndUserId(id, user.getId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        List<Dtos.MemberResponse> members = memberRepository.findByProjectId(id).stream()
                .map(m -> Dtos.MemberResponse.builder()
                        .userId(m.getUser().getId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .role(m.getRole())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{id}/members")
    @Transactional
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                       @RequestBody Dtos.AddMemberRequest req,
                                       @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        // Must be ADMIN
        ProjectMember myMembership = memberRepository.findByProjectIdAndUserId(id, user.getId())
                .orElse(null);
        if (myMembership == null || myMembership.getRole() != ProjectMember.ProjectRole.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can add members"));
        }
        if (memberRepository.existsByProjectIdAndUserId(id, req.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "User is already a member"));
        }
        User newMember = userRepository.findById(req.getUserId())
                .orElse(null);
        if (newMember == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        Project project = projectRepository.findById(id).orElseThrow();
        ProjectMember.ProjectRole role = req.getRole() != null ? req.getRole() : ProjectMember.ProjectRole.MEMBER;
        ProjectMember pm = ProjectMember.builder().project(project).user(newMember).role(role).build();
        memberRepository.save(pm);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Dtos.MemberResponse.builder()
                        .userId(newMember.getId()).name(newMember.getName())
                        .email(newMember.getEmail()).role(role).build()
        );
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Transactional
    public ResponseEntity<?> removeMember(@PathVariable Long id,
                                          @PathVariable Long userId,
                                          @AuthenticationPrincipal UserDetails principal) {
        User user = currentUser(principal);
        ProjectMember myMembership = memberRepository.findByProjectIdAndUserId(id, user.getId()).orElse(null);
        if (myMembership == null || myMembership.getRole() != ProjectMember.ProjectRole.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("error", "Only admins can remove members"));
        }
        if (userId.equals(user.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot remove yourself"));
        }
        memberRepository.deleteByProjectIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }
}
