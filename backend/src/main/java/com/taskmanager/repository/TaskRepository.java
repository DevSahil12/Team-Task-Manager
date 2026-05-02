package com.taskmanager.repository;

import com.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedToId(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId")
    long countByAssignedToId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'IN_PROGRESS'")
    long countInProgressByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.status = 'DONE'")
    long countDoneByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueByUser(@Param("userId") Long userId, @Param("today") LocalDate today);
}
