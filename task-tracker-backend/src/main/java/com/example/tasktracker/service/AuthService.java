package com.example.tasktracker.service;

import com.example.tasktracker.dto.AuthRequest;
import com.example.tasktracker.dto.EmailMessage; 
import com.example.tasktracker.entity.User;
import com.example.tasktracker.repository.UserRepository;
import com.example.tasktracker.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import org.springframework.kafka.core.KafkaTemplate; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j 
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String EMAIL_TOPIC = "EMAIL_SENDING_TASKS";

    public String register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("This email is already taken");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        try {
            EmailMessage welcomeEmail = new EmailMessage(
                    user.getEmail(),
                    "Добро пожаловать в Task Tracker!",
                    "Регистрация прошла успешно"
            );
            
            kafkaTemplate.send(EMAIL_TOPIC, welcomeEmail);
            log.info("Приветственное письмо для {} успешно отправлено в Kafka-топик {}", user.getEmail(), EMAIL_TOPIC);
        } catch (Exception e) {
            log.error("Не удалось отправить сообщение в Kafka для пользователя: {}", user.getEmail(), e);
        }

        return jwtUtils.generateToken(user.getEmail());
    }

    public String login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return jwtUtils.generateToken(user.getEmail());
    }
}