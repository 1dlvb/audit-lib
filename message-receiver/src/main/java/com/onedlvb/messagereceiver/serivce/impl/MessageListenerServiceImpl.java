package com.onedlvb.messagereceiver.serivce.impl;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import com.onedlvb.messagereceiver.repository.KafkaMessageRepository;
import com.onedlvb.messagereceiver.serivce.MessageListenerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageListenerServiceImpl implements MessageListenerService {

    @NonNull
    private final KafkaMessageRepository kafkaMessageRepository;

    @Override
    @KafkaListener(topicPattern = "fintech-topic-.*")
    public void listen(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String message = record.value();
        KafkaMessage kafkaMessage = KafkaMessage.builder()
                .topic(topic)
                .message(message)
                .createDate(LocalDateTime.now())
                .build();
        kafkaMessageRepository.save(kafkaMessage);
    }

}
