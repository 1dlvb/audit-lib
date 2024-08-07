package com.onedlvb.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A component responsible for producing audit messages to Kafka topics.
 * @author Matushkin Anton
 */
@Component
@RequiredArgsConstructor
public class AuditProducer {

    @NonNull
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Sends a message to the specified Kafka topic.
     * Uses transactions.
     * <p>
     * @param topic   the Kafka topic to which the message should be sent
     * @param message the message to be sent, represented as a map of key-value pairs
     */
    public void sendMessage(String topic, Map<String, String> message) {
        kafkaTemplate.executeInTransaction(operations ->
                operations.send(topic, message.toString()));
    }

}
