package com.nexus.authservice.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send verification email using SendGrid
     */
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verifyLink = frontendUrl + "/verify-email?token=" + token;

            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            String subject = "🔐 Verify your Nexus account";
            String contentText = "Welcome to Nexus!\n\n" +
                    "Please verify your email address by clicking the link below:\n\n" +
                    verifyLink + "\n\n" +
                    "This link expires in 24 hours. If you didn't create this account, you can ignore this email.";

            Content content = new Content("text/plain", contentText);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Verification email sent to " + toEmail);
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (IOException e) {
            System.err.println("❌ SendGrid email error: " + e.getMessage());
        }
    }

    /**
     * Send email change verification
     */
    public void sendEmailChangeVerification(String toNewEmail, String token) {
        try {
            String verifyLink = frontendUrl + "/verify-email-change?token=" + token;

            Email from = new Email(fromEmail);
            Email to = new Email(toNewEmail);
            String subject = "📧 Confirm your new email address";
            String contentText = "You requested to change your Nexus account email to this address.\n\n" +
                    "Confirm the change by clicking the link below:\n\n" +
                    verifyLink + "\n\n" +
                    "This link expires in 24 hours. If you didn't request this, you can ignore this email.";

            Content content = new Content("text/plain", contentText);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email change verification sent to " + toNewEmail);
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (IOException e) {
            System.err.println("❌ SendGrid email error: " + e.getMessage());
        }
    }
}