package com.example.task_tracker_scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTaskReportDto {
    private String email;
    private List<String> uncompletedTaskTitles;
    private List<String> completedTodayTaskTitles;
}