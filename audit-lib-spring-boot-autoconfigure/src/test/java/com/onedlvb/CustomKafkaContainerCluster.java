package com.onedlvb;

import lombok.Getter;
import lombok.SneakyThrows;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CustomKafkaContainerCluster implements Startable {

    private final int numberOfBrokers;
    private final Network network;
    private final GenericContainer<?> zookeeperContainer;

    @Getter
    private Collection<KafkaContainer> kafkaBrokers;

    public CustomKafkaContainerCluster(String kafkaVersion, int numberOfBrokers, int replicationFactor) {

        if (numberOfBrokers <= 0) {
            throw new IllegalArgumentException("Number of brokers must be greater than 0");
        }
        if (replicationFactor <= 0 || replicationFactor > numberOfBrokers) {
            throw new IllegalArgumentException("Replication factor must be between 1 and number of brokers");
        }

        this.numberOfBrokers = numberOfBrokers;
        this.network = Network.newNetwork();

        this.zookeeperContainer = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper").withTag(kafkaVersion))
                .withNetwork(network)
                .withNetworkAliases("zk")
                .withEnv("ZOOKEEPER_CLIENT_PORT", String.valueOf(KafkaContainer.ZOOKEEPER_PORT));

        this.kafkaBrokers = IntStream.range(0, this.numberOfBrokers)
                .mapToObj(i -> new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka")
                        .withTag(kafkaVersion))
                        .withNetwork(this.network)
                        .withNetworkAliases("kafka-broker-" + i)
                        .dependsOn(this.zookeeperContainer)
                        .withExternalZookeeper("zk:" + KafkaContainer.ZOOKEEPER_PORT)
                        .withEnv("KAFKA_BROKER_ID", Integer.toString(i))
                        .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", Integer.toString(replicationFactor))
                        .withEnv("KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS", Integer.toString(replicationFactor))
                        .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", Integer.toString(replicationFactor))
                        .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", Integer.toString(replicationFactor))
                        .withStartupTimeout(Duration.ofMinutes(1))
                ).collect(Collectors.toList());

    }

    public String getKafkaBootstrapServers() {
        return kafkaBrokers.stream()
                .map(KafkaContainer::getBootstrapServers)
                .collect(Collectors.joining(","));
    }

    private Stream<GenericContainer<?>> getAllContainers() {
        return Stream.concat(kafkaBrokers.stream(), Stream.of(zookeeperContainer));
    }

    @Override
    @SneakyThrows
    public void start() {
        kafkaBrokers.forEach(GenericContainer::start);

        Unreliables.retryUntilTrue(
                30,
                TimeUnit.SECONDS,
                () -> {
                    Container.ExecResult execResult = zookeeperContainer.execInContainer(
                            "sh",
                            "-c",
                            "zookeeper-shell zk:" + KafkaContainer.ZOOKEEPER_PORT + " ls /brokers/ids | tail -n 1"
                    );
                    String output = execResult.getStdout();
                    return output != null && output.split(",").length == this.numberOfBrokers;
                }
        );
    }

    @Override
    public void stop() {
        getAllContainers().parallel().forEach(GenericContainer::stop);
    }
}