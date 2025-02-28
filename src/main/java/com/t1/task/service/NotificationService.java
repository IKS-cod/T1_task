package com.t1.task.service;


import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomExecutionTime;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.exception.EmailSendingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
    @Value("${spring.mail.toEmail}")
    private String toEmail;
    @Value("${spring.mail.fromEmail}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExecutionTime
    @CustomExceptionHandling
    public void sendSimpleEmail(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new EmailSendingException("Ошибка при отправке электронного письма", e);
        }

    }
}
