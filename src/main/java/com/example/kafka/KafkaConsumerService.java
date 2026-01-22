package com.example.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Kafka Consumer Service with SASL Plain Text Authentication
 */
@ApplicationScoped
public class KafkaConsumerService {

    private static final Logger LOGGER = Logger.getLogger(KafkaConsumerService.class.getName());

    @Inject
    @ConfigProperty(name = "kafka.bootstrap.servers")
    private String bootstrapServers;

    @Inject
    @ConfigProperty(name = "kafka.security.protocol")
    private String securityProtocol;

    @Inject
    @ConfigProperty(name = "kafka.sasl.mechanism")
    private String saslMechanism;

    @Inject
    @ConfigProperty(name = "kafka.sasl.jaas.config")
    private String saslJaasConfig;

    @Inject
    @ConfigProperty(name = "kafka.consumer.key.deserializer")
    private String keyDeserializer;

    @Inject
    @ConfigProperty(name = "kafka.consumer.value.deserializer")
    private String valueDeserializer;

    @Inject
    @ConfigProperty(name = "kafka.consumer.group.id")
    private String groupId;

    @Inject
    @ConfigProperty(name = "kafka.consumer.auto.offset.reset")
    private String autoOffsetReset;

    @Inject
    @ConfigProperty(name = "kafka.consumer.enable.auto.commit")
    private boolean enableAutoCommit;

    @Inject
    @ConfigProperty(name = "kafka.consumer.auto.commit.interval.ms")
    private int autoCommitIntervalMs;

    @Inject
    @ConfigProperty(name = "kafka.consumer.session.timeout.ms")
    private int sessionTimeoutMs;

    @Inject
    @ConfigProperty(name = "kafka.topic.name")
    private String topicName;

    private Consumer<String, String> consumer;
    private ExecutorService executorService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<String> consumedMessages = new ArrayList<>();
    private CompletableFuture<Void> consumerTask;

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing Kafka Consumer with SASL authentication");
        
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, autoCommitIntervalMs);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        
        // SASL Configuration
        props.put("security.protocol", securityProtocol);
        props.put("sasl.mechanism", saslMechanism);
        props.put("sasl.jaas.config", saslJaasConfig);

        try {
            consumer = new KafkaConsumer<>(props);
            executorService = Executors.newSingleThreadExecutor();
            LOGGER.info("Kafka Consumer initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Kafka Consumer: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Kafka Consumer", e);
        }
    }

    /**
     * Start consuming messages from the default topic
     */
    public void startConsuming() {
        startConsuming(topicName);
    }

    /**
     * Start consuming messages from a specific topic
     */
    public void startConsuming(String topic) {
        if (running.get()) {
            LOGGER.warning("Consumer is already running");
            return;
        }

        if (consumer == null) {
            throw new IllegalStateException("Consumer is not initialized");
        }

        running.set(true);
        consumer.subscribe(Arrays.asList(topic));
        
        LOGGER.info("Starting to consume messages from topic: " + topic);

        consumerTask = CompletableFuture.runAsync(() -> {
            try {
                while (running.get()) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                    
                    for (ConsumerRecord<String, String> record : records) {
                        String message = String.format(
                            "Topic: %s, Partition: %d, Offset: %d, Key: %s, Value: %s",
                            record.topic(), record.partition(), record.offset(), 
                            record.key(), record.value()
                        );
                        
                        synchronized (consumedMessages) {
                            consumedMessages.add(message);
                            // Keep only the last 100 messages
                            if (consumedMessages.size() > 100) {
                                consumedMessages.remove(0);
                            }
                        }
                        
                        LOGGER.info("Consumed message: " + message);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    LOGGER.severe("Error in consumer loop: " + e.getMessage());
                }
            }
        }, executorService);
    }

    /**
     * Stop consuming messages
     */
    public void stopConsuming() {
        if (!running.get()) {
            LOGGER.warning("Consumer is not running");
            return;
        }

        LOGGER.info("Stopping consumer");
        running.set(false);
        
        if (consumerTask != null) {
            consumerTask.cancel(true);
        }
        
        if (consumer != null) {
            consumer.wakeup();
        }
    }

    /**
     * Get consumed messages
     */
    public List<String> getConsumedMessages() {
        synchronized (consumedMessages) {
            return new ArrayList<>(consumedMessages);
        }
    }

    /**
     * Clear consumed messages
     */
    public void clearConsumedMessages() {
        synchronized (consumedMessages) {
            consumedMessages.clear();
        }
        LOGGER.info("Consumed messages cleared");
    }

    /**
     * Get consumer status
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Get consumer metrics
     */
    public String getMetrics() {
        if (consumer != null) {
            return consumer.metrics().toString();
        }
        return "Consumer not initialized";
    }

    /**
     * Get consumer group information
     */
    public String getConsumerGroupInfo() {
        return String.format("Group ID: %s, Running: %s, Messages consumed: %d", 
                           groupId, running.get(), consumedMessages.size());
    }

    /**
     * Manually commit offsets
     */
    public void commitSync() {
        if (consumer != null && !enableAutoCommit) {
            consumer.commitSync();
            LOGGER.info("Offsets committed synchronously");
        }
    }

    @PreDestroy
    public void cleanup() {
        LOGGER.info("Cleaning up Kafka Consumer");
        stopConsuming();
        
        if (consumer != null) {
            consumer.close();
        }
        
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}