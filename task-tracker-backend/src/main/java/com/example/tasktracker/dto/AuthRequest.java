package com.example.tasktracker.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}