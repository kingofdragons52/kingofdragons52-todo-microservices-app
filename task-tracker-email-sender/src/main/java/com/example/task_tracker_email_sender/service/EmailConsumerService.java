package com.example.task_tracker_email_sender.service;

import com.example.task_tracker_email_sender.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {

    private final JavaMailSender mailSender;

    @KafkaListener(topics = "EMAIL_SENDING_TASKS", groupId = "email-sender-group")
    public void consumeEmailTask(EmailMessage message) {
        log.info("=================================================");
        log.info("ПЕРЕХВАЧЕНО СООБЩЕНИЕ ИЗ KAFKA!");
        log.info("Кому: {}", message.getTo());
        log.info("Тема: {}", message.getSubject());
        log.info("=================================================");
        
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(message.getTo());
            mail.setSubject(message.getSubject());
            mail.setText(message.getBody());
            mail.setFrom("noreply@tasktracker.com");

            mailSender.send(mail);
            log.info("Письмо для {} успешно отправлено в MailHog!", message.getTo());
        } catch (Exception e) {
            log.error("Не удалось отправить письмо через MailHog: ", e);
        }
    }
}