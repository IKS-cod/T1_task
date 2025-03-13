package com.t1.task.service;

import com.t1.task.config.EmailProperties;
import com.t1.task.exception.EmailSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailProperties emailProperties;

    @BeforeEach
    public void setUp() {
        when(emailProperties.getFromEmail()).thenReturn("test@example.com");
        when(emailProperties.getToEmail()).thenReturn("recipient@example.com");
    }

    @Test
    public void sendSimpleEmailSuccessTest() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";
        NotificationService notificationService = new NotificationService(mailSender, emailProperties);
        // Act
        notificationService.sendSimpleEmail(subject, body);
        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void sendSimpleEmailFailureTest() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new MailException("Failed to send email") {
        }).when(mailSender).send(any(SimpleMailMessage.class));
        NotificationService notificationService = new NotificationService(mailSender, emailProperties);
        // Act
        assertThrows(EmailSendingException.class, () -> notificationService.sendSimpleEmail(subject, body));
        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}