package com.nexus.authservice.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
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
     * Send verification email using SendGrid with improved deliverability
     */
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verifyLink = frontendUrl + "/verify-email?token=" + token;

            Email from = new Email(fromEmail, "Nexus Team");
            Email to = new Email(toEmail);
            
            // HTML version for better deliverability
            String htmlContent = 
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #4CAF50; padding: 20px; text-align: center; color: white; border-radius: 8px 8px 0 0; }" +
                ".content { padding: 30px; background: #f9f9f9; border-radius: 0 0 8px 8px; }" +
                ".button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; }" +
                ".button:hover { background: #45a049; }" +
                ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1>👋 Welcome to Nexus!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello!</p>" +
                "<p>Thank you for creating a Nexus account. Please verify your email address to get started.</p>" +
                "<p style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + verifyLink + "' class='button'>Verify My Account</a>" +
                "</p>" +
                "<p>Or copy and paste this link in your browser:</p>" +
                "<p style='background: #eee; padding: 10px; border-radius: 4px; word-break: break-all;'>" + verifyLink + "</p>" +
                "<p><strong>This link expires in 24 hours.</strong></p>" +
                "<p>If you didn't create this account, you can safely ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>— The Nexus Team</p>" +
                "<p style='font-size: 10px; color: #999;'>You received this email because you signed up for Nexus. If you didn't request this, please ignore.</p>" +
                "</div>" +
                "</body>" +
                "</html>";

            // Plain text version as fallback
            String textContent = 
                "Welcome to Nexus!\n\n" +
                "Thank you for creating a Nexus account. Please verify your email address to get started.\n\n" +
                "Click the link below to verify your account:\n" + verifyLink + "\n\n" +
                "This link expires in 24 hours.\n\n" +
                "If you didn't create this account, you can ignore this email.\n\n" +
                "— The Nexus Team";

            // Send both HTML and Plain Text versions
            Content content = new Content("text/html", htmlContent);
            Content text = new Content("text/plain", textContent);

            Mail mail = new Mail(from, "🔐 Welcome to Nexus – Please verify your email", to, content);
            mail.setReplyTo(new Email(fromEmail));

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
     * Send email change verification with improved formatting
     */
    public void sendEmailChangeVerification(String toNewEmail, String token) {
        try {
            String verifyLink = frontendUrl + "/verify-email-change?token=" + token;

            Email from = new Email(fromEmail, "Nexus Team");
            Email to = new Email(toNewEmail);

            String htmlContent = 
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #4CAF50; padding: 20px; text-align: center; color: white; border-radius: 8px 8px 0 0; }" +
                ".content { padding: 30px; background: #f9f9f9; border-radius: 0 0 8px 8px; }" +
                ".button { display: inline-block; background: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; font-weight: bold; }" +
                ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='header'>" +
                "<h1>📧 Confirm Your New Email</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello!</p>" +
                "<p>You requested to change your Nexus account email to this address.</p>" +
                "<p style='text-align: center; margin: 30px 0;'>" +
                "<a href='" + verifyLink + "' class='button'>Confirm Email Change</a>" +
                "</p>" +
                "<p>Or copy and paste this link in your browser:</p>" +
                "<p style='background: #eee; padding: 10px; border-radius: 4px; word-break: break-all;'>" + verifyLink + "</p>" +
                "<p><strong>This link expires in 24 hours.</strong></p>" +
                "<p>If you didn't request this, you can ignore this email and your email address will stay unchanged.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>— The Nexus Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";

            String textContent = 
                "Confirm Your New Email Address\n\n" +
                "You requested to change your Nexus account email to this address.\n\n" +
                "Click the link below to confirm the change:\n" + verifyLink + "\n\n" +
                "This link expires in 24 hours.\n\n" +
                "If you didn't request this, you can ignore this email.\n\n" +
                "— The Nexus Team";

            Content content = new Content("text/html", htmlContent);
            Content text = new Content("text/plain", textContent);

            Mail mail = new Mail(from, "📧 Confirm your new email address", to, content);
            mail.setReplyTo(new Email(fromEmail));

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