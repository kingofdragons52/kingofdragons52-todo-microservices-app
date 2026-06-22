package com.example.tasktracker.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    @JsonProperty("isDone")
    private boolean isDone;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}