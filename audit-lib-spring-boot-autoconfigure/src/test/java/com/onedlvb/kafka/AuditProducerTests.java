package com.onedlvb.kafka;

import com.onedlvb.CustomKafkaContainerCluster;
import com.onedlvb.advice.exception.KafkaSendMessageException;
import com.onedlvb.config.AuditLibSpringBootStarterAutoConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Testcontainers
@ExtendWith(SpringExtension.class)
@Import(AuditLibSpringBootStarterAutoConfiguration.class)
class AuditProducerTests {

    public static final String TOPIC_NAME_SEND_ORDER = "send-auditlog-event";
    private static String bootstrapServers;
    private static String ports;

    private KafkaConsumer<String, String> consumer;

    @Autowired
    private AuditProducer auditProducer;

    @BeforeAll
    static void setup() {
        CustomKafkaContainerCluster kafka = new CustomKafkaContainerCluster
                ("latest",
                        3,
                        2);
        kafka.start();
        bootstrapServers = kafka.getKafkaBootstrapServers();
        ports = bootstrapServers.split(":")[1];
    }

    @BeforeEach
    void setupConsumer() {

        consumer = new KafkaConsumer<>(ImmutableMap.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-id",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(),
                new StringDeserializer());

        consumer.subscribe(Collections.singletonList(TOPIC_NAME_SEND_ORDER));

    }

    @AfterEach
    void teardown() throws IOException {
        Runtime.getRuntime().exec(String.format("sudo iptables -D OUTPUT -p tcp --dport %s -j DROP", ports));
        consumer.close();
    }

    @Test
    void testAuditProducerSavesMessageToKafkaBrokerToReplicas() throws KafkaSendMessageException {
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        auditProducer.sendMessage(TOPIC_NAME_SEND_ORDER, message);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));

        Map<Integer, List<String>> partitionMessages = new HashMap<>();
        records.forEach(record ->
                partitionMessages.computeIfAbsent(record.partition(), k -> new ArrayList<>()).add(record.value()));

        for (int partition : partitionMessages.keySet()) {
            assertThat(partitionMessages.get(partition)).isNotEmpty();
        }
    }

    @Test
    void testAuditProducerReturnsSavedDataAfterRecoveryOfBrokerFailure()
            throws InterruptedException, KafkaSendMessageException, IOException {
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));
        assertEquals(0, records.count());

        // Blocking connections for port
        Runtime.getRuntime().exec(String.format("sudo iptables -A OUTPUT -p tcp --dport %s -j DROP", ports));

        Thread.sleep(10000);

        setField(auditProducer, "bootstrapServers", bootstrapServers);
        auditProducer.sendMessage(TOPIC_NAME_SEND_ORDER, message);

        // Undo blocking connections for port
        Runtime.getRuntime().exec(String.format("sudo iptables -D OUTPUT -p tcp --dport %s -j DROP", ports));

        Thread.sleep(10000);

        ConsumerRecords<String, String> newRecords = consumer.poll(Duration.ofMillis(10000L));

        assertTrue(newRecords.count() > 0);
        for (ConsumerRecord<String, String> record : newRecords) {
            assertThat(record.value()).contains("key1", "val1");
            assertThat(record.value()).contains("key2", "val2");
            assertThat(record.value()).contains("key3", "val3");
        }
    }

    @Test
    void testAuditProducerSavesMessageToKafkaBroker() throws KafkaSendMessageException {
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        setField(auditProducer, "bootstrapServers", bootstrapServers);
        auditProducer.sendMessage(TOPIC_NAME_SEND_ORDER, message);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));

        assertEquals(1, records.count());
        for (ConsumerRecord<String, String> record : records) {
            assertThat(record.value()).contains("key1", "val1");
            assertThat(record.value()).contains("key2", "val2");
            assertThat(record.value()).contains("key3", "val3");
        }
    }

}
