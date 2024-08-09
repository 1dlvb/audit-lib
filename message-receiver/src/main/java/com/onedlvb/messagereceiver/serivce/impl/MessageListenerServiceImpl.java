package com.onedlvb.messagereceiver.serivce.impl;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import com.onedlvb.messagereceiver.repository.KafkaMessageRepository;
import com.onedlvb.messagereceiver.serivce.MessageListenerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * An implementation of {@link MessageListenerService} interface.
 * @author Matushkin Anton
 */
@Service
@RequiredArgsConstructor
public class MessageListenerServiceImpl implements MessageListenerService {

    @NonNull
    private final KafkaMessageRepository kafkaMessageRepository;

    @Override
    @KafkaListener(id = "MessageListener", topicPattern = "fintech-topic-.*")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        String topic = record.topic();
        String message = record.value();
        KafkaMessage kafkaMessage = KafkaMessage.builder()
                .topic(topic)
                .message(message)
                .createDate(LocalDateTime.now())
                .build();
        kafkaMessageRepository.save(kafkaMessage);
        acknowledgment.acknowledge();
    }

}
