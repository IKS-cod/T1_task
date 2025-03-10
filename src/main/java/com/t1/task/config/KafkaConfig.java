package com.t1.task.config;

import com.t1.task.dto.TaskUpdateStatusDto;
import com.t1.task.kafka.KafkaTaskProducer;
import com.t1.task.kafka.MessageDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${t1.kafka.consumer.group-id}")
    private String groupId;
    @Value("${t1.kafka.bootstrap.server}")
    private String servers;
    @Value("${t1.kafka.session.timeout.ms:15000}")
    private String sessionTimeout;
    @Value("${t1.kafka.max.partition.fetch.bytes:300000}")
    private String maxPartitionFetchBytes;
    @Value("${t1.kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("${t1.kafka.max.poll.interval.ms:3000}")
    private String maxPollIntervalsMs;
    @Value("${t1.kafka.topic.client_id_registered}")
    private String taskTopic;
    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public ConsumerFactory<String, TaskUpdateStatusDto> consumerListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers); // сервер
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // консюмер группа
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // кто будет десериализовать ключ
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class); // кто будет десериализовать value
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.t1.task.dto.TaskUpdateStatusDto"); // во что маппим? где это взять
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*"); //если маппим во что-то вне пакета - кафна не сделает этого,
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // заголовки
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes); // максимальный размер сообщения, если не уложится буде
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // сколько сообщений прочитать за один раз и коммит одного оффсета
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalsMs); // время сколько консьюмер может получать ответ от кафки и рабо
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE); // консьюмер будет ли автоматически подтверждать смещение после обрабо
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // начинать с раннего сообщения
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class.getName()); // для ошибок свой десериализатор
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, MessageDeserializer.class); // для ошибок свой десериализатор


        DefaultKafkaConsumerFactory<String, TaskUpdateStatusDto> factory = new DefaultKafkaConsumerFactory<>(props);
        factory.setKeyDeserializer(new StringDeserializer());
        return factory;

    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TaskUpdateStatusDto> kafkaListenerContainerFactory(@Qualifier("consumerListenerFactory") ConsumerFactory<String, TaskUpdateStatusDto> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, TaskUpdateStatusDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryBuilder(consumerFactory, factory);
        return factory;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory,
                                    ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler());
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        handler.addNotRetryableExceptions(IllegalStateException.class);
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
        {
            logger.error(" Retry Listeners message = {}, offset = {} delivery Attempt = {}",
                    ex.getMessage(), record.offset(), deliveryAttempt);
        });
        return handler;
    }

    @Bean("task")
    public KafkaTemplate<String, TaskUpdateStatusDto> kafkaTemplate(ProducerFactory<String,
            TaskUpdateStatusDto> producerPatFactory) {
        return new KafkaTemplate<>(producerPatFactory);
    }

    @Bean
    @ConditionalOnProperty(value = "t1.kafka.producer.enable",
            havingValue = "true",
            matchIfMissing = true)
    public KafkaTaskProducer producerTask(@Qualifier("task") KafkaTemplate template) {
        template.setDefaultTopic(taskTopic);
        return new KafkaTaskProducer(template);
    }

    @Bean
    public ProducerFactory<String, TaskUpdateStatusDto> producerTaskFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);

    }
}