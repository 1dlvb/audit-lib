package com.onedlvb.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditProducerTests {


    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private AuditProducer auditProducer;


    @Test
    void testSendMessageReturnsProperValue() {
        String topic = "test-topic";
        Map<String, String> message = new HashMap<>();
        message.put("key1", "value1");
        message.put("key2", "value2");

        KafkaOperations<String, String> operations = mock(KafkaOperations.class);
        when(kafkaTemplate.executeInTransaction(any())).thenAnswer(invocationOnMock -> {
            KafkaOperations.OperationsCallback<String, String, Object> callback = invocationOnMock.getArgument(0);
            return callback.doInOperations(operations);
        });

        auditProducer.sendMessage(topic, message);
        verify(operations).send(eq(topic), eq(message.toString()));

    }

}

