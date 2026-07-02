package com.nexus.authservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your Nexus account");
        message.setText(
                "Welcome to Nexus!\n\n" +
                "Please verify your email address by clicking the link below:\n\n" +
                verifyLink + "\n\n" +
                "This link expires in 24 hours. If you didn't create this account, you can ignore this email."
        );

        mailSender.send(message);
    }

    public void sendEmailChangeVerification(String toNewEmail, String token) {
        String verifyLink = frontendUrl + "/verify-email-change?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toNewEmail);
        message.setSubject("Confirm your new email address");
        message.setText(
                "You requested to change your Nexus account email to this address.\n\n" +
                "Confirm the change by clicking the link below:\n\n" +
                verifyLink + "\n\n" +
                "This link expires in 24 hours. If you didn't request this, you can ignore this email " +
                "and your email address will stay unchanged."
        );

        mailSender.send(message);
    }
}
