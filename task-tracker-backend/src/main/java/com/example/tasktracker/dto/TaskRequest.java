package com.example.tasktracker.dto;

import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;
    private boolean isDone;
}