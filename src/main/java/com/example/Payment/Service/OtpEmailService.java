package com.example.Payment.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Service
public class OtpEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String from;

    @Async("mailExecutor")
    public CompletableFuture<Void> sendOtpAsync(String toEmail, int code) {
        sendOtp(toEmail, code); // твой текущий sendOtp (SimpleMailMessage)
        return CompletableFuture.completedFuture(null);
    }

    public OtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String toEmail, int code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setFrom(from);
        msg.setSubject("DemoShop — код подтверждения");
        msg.setText("Ваш одноразовый код подтверждения: " + code + "\n\n"
                + "Если вы не запрашивали оплату — просто игнорируйте это письмо.");
        mailSender.send(msg);
    }
}
