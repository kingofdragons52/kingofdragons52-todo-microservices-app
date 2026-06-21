package com.example.task_tracker_scheduler.service;

import com.example.task_tracker_scheduler.dto.EmailMessage;
import com.example.task_tracker_scheduler.dto.UserTaskReportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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

    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(fixedRate = 30000)
    public void sendDailyTaskReports() {
        log.info("Планировщик запущен: запрашиваем ежедневные отчеты у бэкенда...");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", "my-super-shared-secret-key-2026");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<UserTaskReportDto>> response = restTemplate.exchange(
                    backendUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<UserTaskReportDto>>() {}
            );

            List<UserTaskReportDto> reports = response.getBody();

            if (reports == null || reports.isEmpty()) {
                log.info("Бэкенд не вернул отчетов для обработки.");
                return;
            }

            for (UserTaskReportDto report : reports) {
                List<String> uncompleted = report.getUncompletedTaskTitles();
                List<String> completed = report.getCompletedTodayTaskTitles();

                boolean hasUncompleted = uncompleted != null && !uncompleted.isEmpty();
                boolean hasCompleted = completed != null && !completed.isEmpty();

                if (!hasUncompleted && !hasCompleted) {
                    continue;
                }

                String emailSubject = determineSubject(hasUncompleted, hasCompleted);
                String emailBody = buildEmailBody(uncompleted, completed);

                EmailMessage message = new EmailMessage(
                        report.getEmail(),
                        emailSubject,
                        emailBody
                );

                kafkaTemplate.send("EMAIL_SENDING_TASKS", message);
                log.info("Отчет для {} успешно отправлен в Kafka. Статус: Выполнено за сутки [{}], Осталось [{}]", 
                        report.getEmail(), 
                        hasCompleted ? completed.size() : 0, 
                        hasUncompleted ? uncompleted.size() : 0);
            }

        } catch (Exception e) {
            log.error("Ошибка во время работы планировщика отчетов: ", e);
        }
    }

    private String determineSubject(boolean hasUncompleted, boolean hasCompleted) {
        if (hasUncompleted && hasCompleted) {
            return "Ежедневный отчет: выполненные и оставшиеся задачи";
        } else if (hasCompleted) {
            return "Отличная работа! Ваши выполненные задачи за сегодня";
        } else {
            return "Внимание! У вас остались невыполненные задачи";
        }
    }

    private String buildEmailBody(List<String> uncompleted, List<String> completed) {
        StringBuilder sb = new StringBuilder();
        sb.append("Привет! Вот твой отчет по задачам за прошедшие сутки:\n\n");

        if (completed != null && !completed.isEmpty()) {
            sb.append("✓ За сегодня вы выполнили задач: ").append(completed.size()).append("\n");
            int limit = Math.min(completed.size(), 5);
            for (int i = 0; i < limit; i++) {
                sb.append("   - ").append(completed.get(i)).append("\n");
            }
            if (completed.size() > 5) {
                sb.append("   - и еще ").append(completed.size() - 5).append(" шт.\n");
            }
            sb.append("\n");
        }

        if (uncompleted != null && !uncompleted.isEmpty()) {
            sb.append("У вас осталось невыполненных задач: ").append(uncompleted.size()).append("\n");
            int limit = Math.min(uncompleted.size(), 5);
            for (int i = 0; i < limit; i++) {
                sb.append("   - ").append(uncompleted.get(i)).append("\n");
            }
            if (uncompleted.size() > 5) {
                sb.append("   - и еще ").append(uncompleted.size() - 5).append(" шт.\n");
            }
            sb.append("\nНе откладывай на потом!");
        } else if (completed != null && !completed.isEmpty()) {
            sb.append("Потрясающе! Все задачи на сегодня закрыты. Так держать! 🚀");
        }

        return sb.toString();
    }
}