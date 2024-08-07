package com.onedlvb.messagereceiver.serivce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Service interface for processing incoming Kafka messages.
 * @author Matushkin Anton
 */
public interface MessageListenerService {

    /**
     * Processes a Kafka message received from a topic.
     *
     * <p>
     * The method is invoked for each message consumed from the Kafka topic.
     * </p>
     *
     * @param record the Kafka {@link ConsumerRecord} containing the message details
     */
    void listen(ConsumerRecord<String, String> record);

}
