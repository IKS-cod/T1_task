package com.t1.task.kafka;

import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.dto.TaskUpdateStatusDto;
import com.t1.task.exception.KafkaMessageProcessingException;
import com.t1.task.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTaskConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTaskConsumer.class);
    private final NotificationService notificationService;

    public KafkaTaskConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(id = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.task_update}",
            containerFactory = "kafkaListenerContainerFactory")
    @CustomLoggingStartMethod
    @CustomLoggingFinishedMethod
    @CustomExceptionHandling
    public void listener(@Payload TaskUpdateStatusDto taskUpdateStatusDto,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            String subject = "Изменение статуса Task c id= " + taskUpdateStatusDto.getId();
            String text = "Статус Task стал - " + taskUpdateStatusDto.getStatus();
            notificationService.sendSimpleEmail(subject, text);
        } catch (Exception e) {
            throw new KafkaMessageProcessingException("Ошибка при обработке сообщения Kafka", e);
        } finally {
            ack.acknowledge();
        }
    }
}