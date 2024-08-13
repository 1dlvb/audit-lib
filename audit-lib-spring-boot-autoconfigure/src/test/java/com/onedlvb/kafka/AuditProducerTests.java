package com.onedlvb.kafka;

import com.onedlvb.advice.exception.KafkaSendMessageException;
import com.onedlvb.config.AuditLibSpringBootStarterAutoConfiguration;
import com.onedlvb.util.CustomKafkaContainerCluster;
import com.onedlvb.util.SpringContextRestartExtension;
import com.onedlvb.util.SpringRestarter;
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@Testcontainers
@SpringJUnitConfig
@Import(AuditLibSpringBootStarterAutoConfiguration.class)
@ExtendWith({SpringExtension.class, SpringContextRestartExtension.class})
class AuditProducerTests {

    private static final String TOPIC = "fintech-topic-test";
    private static String bootstrapServers;
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
    }

    @BeforeEach
    void setupConsumer() {

        consumer = new KafkaConsumer<>(ImmutableMap.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, "test-group-id",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"),
                new StringDeserializer(),
                new StringDeserializer());

        consumer.subscribe(Collections.singletonList(TOPIC));

    }

    @AfterEach
    void teardown() {
        consumer.close();
    }

    @Test
    void testAuditProducerSavesMessageToKafkaBrokerToReplicas() throws KafkaSendMessageException {
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        auditProducer.sendMessage(TOPIC, message);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));

        Map<Integer, List<String>> partitionMessages = new HashMap<>();
        records.forEach(record ->
                partitionMessages.computeIfAbsent(record.partition(), k -> new ArrayList<>()).add(record.value()));

        for (int partition : partitionMessages.keySet()) {
            assertThat(partitionMessages.get(partition)).isNotEmpty();
        }
    }

    @Test
    void testAuditProducerReturnsSavedDataAfterRecoveryOfBrokerFailure() {

        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));
        assertEquals(0, records.count());
        SpringRestarter.getInstance().restart(() -> {
            setField(auditProducer, "bootstrapServers", bootstrapServers);
            try {
                auditProducer.sendMessage(TOPIC, message);
            } catch (KafkaSendMessageException e) {
                throw new RuntimeException(e);
            }
        });

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ConsumerRecords<String, String> newRecords = consumer.poll(Duration.ofMillis(10000L));
            boolean containsMessage = false;
            for (ConsumerRecord<String, String> record : newRecords) {
                if (record.value().contains("key1") && record.value().contains("val1") &&
                        record.value().contains("key2") && record.value().contains("val2") &&
                        record.value().contains("key3") && record.value().contains("val3")) {
                    containsMessage = true;
                }
            }
            assertTrue(containsMessage);
        });

    }

    @Test
    void testAuditProducerSavesMessageToKafkaBroker() throws KafkaSendMessageException {
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3"
        );

        setField(auditProducer, "bootstrapServers", bootstrapServers);
        auditProducer.sendMessage(TOPIC, message);

        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000L));

        assertEquals(1, records.count());
        for (ConsumerRecord<String, String> record : records) {
            assertThat(record.value()).contains("key1", "val1");
            assertThat(record.value()).contains("key2", "val2");
            assertThat(record.value()).contains("key3", "val3");
        }
    }

}
