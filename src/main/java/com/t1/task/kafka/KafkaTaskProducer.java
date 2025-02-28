package com.t1.task.kafka;

import com.t1.task.aspect.annotation.CustomExceptionHandling;
import com.t1.task.aspect.annotation.CustomLoggingFinishedMethod;
import com.t1.task.aspect.annotation.CustomLoggingStartMethod;
import com.t1.task.exception.KafkaMessageSendingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTaskProducer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaTaskProducer.class);
    private final KafkaTemplate template;

    public KafkaTaskProducer(KafkaTemplate template) {
        this.template = template;
    }
//    @CustomLoggingStartMethod
//    @CustomLoggingFinishedMethod
//    @CustomExceptionHandling
    public void sendTo(String topic, Object o) {
        try {
            template.send(topic, o);
        } catch (Exception ex) {
            throw new KafkaMessageSendingException("Ошибка при отправке сообщения в Kafka", ex);
        }
    }
}
