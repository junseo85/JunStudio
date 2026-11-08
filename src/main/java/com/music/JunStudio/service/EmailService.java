package com.music.JunStudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends the plain-text transactional messages used by scheduling and account workflows. */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // A generic method to send any simple text email
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        // This should match the spring.mail.username in your properties file
        message.setFrom("85junseo@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        // Fire it off!
        mailSender.send(message);
    }
}