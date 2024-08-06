package com.onedlvb.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditProducer {

    @NonNull
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, Map<String, String> message) {
        kafkaTemplate.executeInTransaction(operations ->
                operations.send(topic, message.toString()));
    }

}
