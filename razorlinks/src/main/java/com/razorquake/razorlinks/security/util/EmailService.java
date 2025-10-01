package com.razorquake.razorlinks.security.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String resetUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Password Reset Request - RazorLinks");

        String htmlContent = "<html><body>" +
                "<p>Click the link below to reset your password:</p>" +
                "<p><a href=\"" + resetUrl + "\">Reset Password</a></p>" +
                "<p>This link will expire in 1 hour.</p>" +
                "<p>If you didn't initiate a password reset request, please ignore this email.</p>" +
                "<p>Best regards,<br>The RazorLinks Team</p>" +
                "</body></html>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String verificationUrl) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Email Verification - RazorLinks");
        String htmlContent = "<html><body>" +
                "<p>Welcome to RazorLinks!</p>" +
                "<p>Please verify your email address by clicking the link below:</p>" +
                "<p><a href=\"" + verificationUrl + "\">Verify Email</a></p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>If you didn't create an account with us, please ignore this email.</p>" +
                "<p>Best regards,<br>The RazorLinks Team</p>" +
                "</body></html>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
