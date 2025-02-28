package com.t1.task.kafka;

import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class MessageDeserializer<T> extends JsonDeserializer<T> {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

    private static String getMessage(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            return super.deserialize(topic, headers, data);
        } catch (Exception e) {
            logger.warn("Произошла ошибка во время десериализации сообщения {}", new String(data, StandardCharsets.UTF_8), e);
            return null;
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return super.deserialize(topic, data);
        } catch (Exception e) {
            logger.warn("Произошла ошибка во время десериализации сообщения {}", new String(data, StandardCharsets.UTF_8), e);
            return null;
        }
    }
}
