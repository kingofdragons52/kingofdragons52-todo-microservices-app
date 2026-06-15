package com.example.tasktracker.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private boolean isDone;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}