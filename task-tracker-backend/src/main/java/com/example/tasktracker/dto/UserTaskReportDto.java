package com.example.tasktracker.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskReportDto {
    private String email;
    private List<String> uncompletedTaskTitles;
}
