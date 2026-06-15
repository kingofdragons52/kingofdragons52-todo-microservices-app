package com.example.task_tracker_email_sender.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {
    private String to;
    private String subject;
    private String body;
}