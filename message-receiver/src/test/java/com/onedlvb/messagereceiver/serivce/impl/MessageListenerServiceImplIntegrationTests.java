package com.onedlvb.messagereceiver.serivce.impl;

import com.onedlvb.messagereceiver.model.KafkaMessage;
import com.onedlvb.messagereceiver.repository.KafkaMessageRepository;
import com.onedlvb.messagereceiver.util.CustomKafkaContainerCluster;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class MessageListenerServiceImplIntegrationTests {

    @Autowired
    private KafkaMessageRepository kafkaMessageRepository;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    private MessageListenerContainer consumer;

    private static String bootstrapServers;

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgreSQLContainer::getDriverClassName);
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
    }

    @BeforeAll
    static void setUp() {
        CustomKafkaContainerCluster cluster = new CustomKafkaContainerCluster(
                "latest",
                1,
                1);
        cluster.start();
        bootstrapServers = cluster.getKafkaBootstrapServers();
        System.setProperty("spring.kafka.bootstrap-servers", bootstrapServers);
    }

    @AfterEach
    void cleanup() {
        kafkaMessageRepository.deleteAll();
    }

    @BeforeEach
    void setupConsumer() {
        consumer = kafkaListenerEndpointRegistry.getListenerContainer("MessageListener");
        kafkaMessageRepository.deleteAll();
    }

    @Test
    void testConsumerReturnsProperSavedMessage() {
        consumer.start();
        Map<String, String> message = Map.of(
                "key1", "val1",
                "key2", "val2",
                "key3", "val3",
                "key4", "val4"
        );

        sendMessage(message);
        List<KafkaMessage> messages  = kafkaMessageRepository.findAll();
        for (KafkaMessage m: messages) {
            assertEquals(m.getMessage(), message.toString());
            assertEquals(m.getTopic(), "fintech-topic-test");
        }
        consumer.stop();

    }

    @Test
    void testConsumerHandlesMessagesCorrectlyAfterRestart() {
        // imitating consumer failure
        consumer.stop();
        Map<String, String> message1 = new LinkedHashMap<>();
        message1.put("key1", "val1");
        message1.put("key2", "val2");
        message1.put("key3", "val3");
        message1.put("key4", "val4");

        Map<String, String> message2 = new LinkedHashMap<>();
        message2.put("key21", "val21");
        message2.put("key22", "val22");
        message2.put("key23", "val23");
        message2.put("key24", "val24");

        sendMessage(message1, message2);

        // undo imitating consumer failure
        consumer.start();

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(5, TimeUnit.SECONDS).until(() -> true);

        List<KafkaMessage> messages = kafkaMessageRepository.findAll();
        assertTrue(messages.get(0).getMessage().contains("key1"));
        assertTrue(messages.get(1).getMessage().contains("key21"));
        assertEquals(2, messages.size());

    }

    @SafeVarargs
    private void sendMessage(Map<String, String> ... messages) {
        KafkaTemplate<String, String> template = kafkaTemplate(bootstrapServers);
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(5, TimeUnit.SECONDS).until(() -> true);
        for (Map<String, String> message: messages) {
            template.send("fintech-topic-test", String.valueOf(message));
        }
        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollDelay(5, TimeUnit.SECONDS).until(() -> true);
    }

    private static KafkaTemplate<String, String> kafkaTemplate(String bootstrapServers) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        return new KafkaTemplate<>(producerFactory);
    }

}

