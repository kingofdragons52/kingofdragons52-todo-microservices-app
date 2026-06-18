package com.example.tasktracker.service;

import org.springframework.transaction.annotation.Transactional;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskResponse;
import com.example.tasktracker.dto.UserTaskReportDto;
import com.example.tasktracker.entity.Task;
import com.example.tasktracker.entity.User;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Map;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .isDone(task.isDone())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    public List<TaskResponse> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse createTask(TaskRequest request) {
        User user = getCurrentUser();
        
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isDone(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        User user = getCurrentUser();
        
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found or access denied"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        
        if (request.isDone() && !task.isDone()) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (!request.isDone()) {
            task.setCompletedAt(null);
        }
        
        task.setDone(request.isDone());

        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found or access denied"));
        
        taskRepository.delete(task);
    }

    public List<UserTaskReportDto> getUncompletedTasksReport() {
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusDays(1);

        return userRepository.findAll().stream()
                .map(user -> {
                    List<String> uncompletedTitles = user.getTasks().stream()
                            .filter(task -> !task.isDone()) 
                            .map(com.example.tasktracker.entity.Task::getTitle)
                            .toList();

                    List<String> completedTodayTitles = user.getTasks().stream()
                            .filter(task -> task.isDone() && task.getCompletedAt() != null && task.getCompletedAt().isAfter(twentyFourHoursAgo))
                            .map(com.example.tasktracker.entity.Task::getTitle)
                            .toList();

                    return new UserTaskReportDto(user.getEmail(), uncompletedTitles, completedTodayTitles);
                })
                .filter(report -> !report.getUncompletedTaskTitles().isEmpty() || !report.getCompletedTodayTaskTitles().isEmpty())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserTaskReportDto> getDailyTasksReport() {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);

        List<Task> tasks = taskRepository.findTasksForDailyReport(dayAgo);

        Map<User, List<Task>> tasksByUser = tasks.stream()
                .collect(Collectors.groupingBy(Task::getUser));

        List<UserTaskReportDto> reports = new ArrayList<>();

        for (Map.Entry<User, List<Task>> entry : tasksByUser.entrySet()) {
            User user = entry.getKey();
            List<Task> userTasks = entry.getValue();

            List<String> uncompletedTitles = userTasks.stream()
                    .filter(task -> !task.isDone())
                    .map(Task::getTitle)
                    .collect(Collectors.toList());

            List<String> completedTitles = userTasks.stream()
                    .filter(Task::isDone)
                    .map(Task::getTitle)
                    .collect(Collectors.toList());

            if (!uncompletedTitles.isEmpty() || !completedTitles.isEmpty()) {
                reports.add(new UserTaskReportDto(
                        user.getEmail(),
                        uncompletedTitles,
                        completedTitles
                ));
            }
        }

        return reports;
    }
}