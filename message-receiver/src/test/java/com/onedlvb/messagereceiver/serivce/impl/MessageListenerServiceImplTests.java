package com.onedlvb.messagereceiver.serivce.impl;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import com.onedlvb.messagereceiver.repository.KafkaMessageRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageListenerServiceImplTests {

    @Mock
    private KafkaMessageRepository kafkaMessageRepository;
    @InjectMocks
    private MessageListenerServiceImpl messageListenerService;
    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void testListenReceivesAndSavesMessages() {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(
                "fintech-topic-test",
                0,
                0L,
                "key1",
                "value1");

        messageListenerService.listen(record, acknowledgment);

        ArgumentCaptor<KafkaMessage> captor = ArgumentCaptor.forClass(KafkaMessage.class);
        verify(kafkaMessageRepository).save(captor.capture());

        KafkaMessage capturedMessage = captor.getValue();

        assertEquals("fintech-topic-test", capturedMessage.getTopic());
        assertEquals("value1", capturedMessage.getMessage());
        assertNotNull(capturedMessage.getCreateDate());
        verify(acknowledgment).acknowledge();
    }

}