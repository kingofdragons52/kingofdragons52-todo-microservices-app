package com.example.task_tracker_email_sender.service; 

import com.example.task_tracker_email_sender.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailConsumerService {

    @KafkaListener(topics = "EMAIL_SENDING_TASKS", groupId = "email-sender-group")
    public void consumeEmailTask(EmailMessage message) {
        log.info("=================================================");
        log.info("ПЕРЕХВАЧЕНО СООБЩЕНИЕ ИЗ KAFKA!");
        log.info("Кому: {}", message.getTo());
        log.info("Тема: {}", message.getSubject());
        log.info("Текст письма: {}", message.getBody());
        log.info("=================================================");
        
    }
}