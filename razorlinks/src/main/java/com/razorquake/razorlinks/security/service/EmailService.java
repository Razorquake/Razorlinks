package com.razorquake.razorlinks.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;


    public void sendPasswordResetEmail(String to, String resetUrl){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("Click the link to reset your password: " + resetUrl);
        mailSender.send(message);
    }

    public void sendVerificationEmail(String to, String verificationUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Email Verification - RazorLinks");
        message.setText(
                "Welcome to RazorLinks!\n\n" +
                        "Please verify your email address by clicking the link below:\n" +
                        verificationUrl + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't create an account with us, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The RazorLinks Team"
        );
        mailSender.send(message);
    }
}
