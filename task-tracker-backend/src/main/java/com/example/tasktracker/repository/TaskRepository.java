package com.example.tasktracker.repository;

import com.example.tasktracker.entity.User;
import com.example.tasktracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE t.isDone = false OR (t.isDone = true AND t.completedAt >= :since)")
    List<Task> findTasksForDailyReport(@Param("since") LocalDateTime since);
}