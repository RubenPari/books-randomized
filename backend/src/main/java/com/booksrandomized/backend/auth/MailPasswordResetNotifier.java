package com.booksrandomized.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
class MailPasswordResetNotifier implements PasswordResetNotifier {
    private final JavaMailSender mail;
    private final String from;

    MailPasswordResetNotifier(JavaMailSender mail,
            @Value("${auth.password-reset.from}") String from) {
        this.mail = mail;
        this.from = from;
    }

    @Override
    public void send(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Books/Randomized password reset");
        message.setText("Use this one-time password reset token: " + token);
        mail.send(message);
    }
}
