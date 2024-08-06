package com.onedlvb.messagereceiver.serivce;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MessageListenerService {

    void listen(ConsumerRecord<String, String> record);

}
