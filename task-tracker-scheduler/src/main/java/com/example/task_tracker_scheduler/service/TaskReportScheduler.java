package com.example.task_tracker_scheduler.service;

import com.example.task_tracker_scheduler.dto.EmailMessage;
import com.example.task_tracker_scheduler.dto.UserTaskReportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskReportScheduler {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Scheduled(fixedRate = 30000)
    public void sendDailyTaskReports() {
        log.info("Планыровщик запущен: запрашиваем отчеты у бэкенда...");

        try {
            ResponseEntity<List<UserTaskReportDto>> response = restTemplate.exchange(
                    backendUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UserTaskReportDto>>() {}
            );

            List<UserTaskReportDto> reports = response.getBody();

            if (reports == null || reports.isEmpty()) {
                log.info("Нет невыполненных задач. Отчеты отправлять никому не нужно.");
                return;
            }

            for (UserTaskReportDto report : reports) {
                String emailBody = buildEmailBody(report.getUncompletedTaskTitles());
                EmailMessage message = new EmailMessage(
                        report.getEmail(),
                        "Внимание! Ваши невыполненные задачи",
                        emailBody
                );

                kafkaTemplate.send("EMAIL_SENDING_TASKS", message);
                log.info("Отчет для {} успешно отправлен в Kafka", report.getEmail());
            }

        } catch (Exception e) {
            log.error("Ошибка во время работы планировщика отчетов: ", e);
        }
    }

    private String buildEmailBody(List<String> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("Привет! Напоминаем, что у тебя остались невыполненные задачи:\n\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append(i + 1).append(". ").append(tasks.get(i)).append("\n");
        }
        sb.append("\nНе откладывай на потом!");
        return sb.toString();
    }
}