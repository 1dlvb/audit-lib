package com.onedlvb.kafka;

import com.onedlvb.advice.exception.KafkaSendMessageException;
import com.onedlvb.config.AuditLibProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

/**
 * A component responsible for producing audit messages to Kafka topics.
 * @author Matushkin Anton
 */
@Component
@RequiredArgsConstructor
public class AuditProducer {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @NonNull
    private final AuditLibProperties properties;

    /**
     * Sends a message to the specified Kafka topic.
     * Uses transactions.
     * <p>
     * @param topic   the Kafka topic to which the message should be sent
     * @param message the message to be sent, represented as a map of key-value pairs
     */
    public void sendMessage(String topic, Map<String, String> message) throws KafkaSendMessageException {
        KafkaProducer<String, String> producer = null;
        try {
            producer = getKafkaProducer();
            producer.beginTransaction();
            producer.send(new ProducerRecord<>(topic, message.toString()));
            producer.commitTransaction();
        } catch (ProducerFencedException e) {
            if (producer != null) {
                producer.close();
            }
        } catch (KafkaException e) {
            if (producer != null) {
                producer.abortTransaction();
            }
            throw new KafkaSendMessageException("Unable to send message to kafka broker.");
        }

    }

    /**
     * @return configured producer.
     */
    private KafkaProducer<String, String> getKafkaProducer() {
        String defaultTransactionalId = "default-transactional-id";
        if (properties.getKafkaTransactionalId() != null) {
            defaultTransactionalId = properties.getKafkaTransactionalId();
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("enable.idempotence", "true");
        props.put("transactional.id", defaultTransactionalId);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.block.ms", "5000");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.initTransactions();
        return producer;
    }

}
