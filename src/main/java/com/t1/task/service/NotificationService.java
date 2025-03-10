package com.t1.task.service;


import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomExecutionTime;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.config.EmailProperties;
import com.t1.task.exception.EmailSendingException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    public NotificationService(JavaMailSender mailSender, EmailProperties emailProperties) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExecutionTime
    @CustomExceptionHandling
    public void sendSimpleEmail(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailProperties.getFromEmail());
        message.setTo(emailProperties.getToEmail());
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException("Ошибка при отправке электронного письма", e);
        }
    }
}

