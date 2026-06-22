package com.example.tasktracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TaskRequest {
    private String title;
    private String description;

    @JsonProperty("isDone")
    private boolean isDone;
}