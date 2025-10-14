package com.razorquake.razorlinks.security.util;

import com.razorquake.razorlinks.exception.EmailVerificationException;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendPasswordResetEmail(String to, String username, String resetUrl) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("resetUrl", resetUrl);
            params.put("username", username);

            String htmlContent = renderTemplate("emails/password-reset.jte", params);
            sendEmail(to, "Password Reset Request - RazorLinks", htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new EmailVerificationException("Failed to send password reset email. Please try again later.");
        }

    }

    private String renderTemplate(String templatePath, Map<String, Object> params) {
        StringOutput output = new StringOutput();
        try {
            templateEngine.render(templatePath, params, output);
        } catch (Exception e) {
            log.error("Failed to render template: {}", templatePath, e);
            throw new EmailVerificationException("Failed to generate email content. Please try again later.");
        }
        return output.toString();
    }

    public void sendVerificationEmail(String to, String username, String verificationUrl) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("verificationUrl", verificationUrl);
            params.put("username", username);

            String htmlContent = renderTemplate("emails/email-verification.jte", params);
            sendEmail(to, "Email Verification - RazorLinks", htmlContent);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new EmailVerificationException("Failed to send verification email. Please try again later.");
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);

    }
}
