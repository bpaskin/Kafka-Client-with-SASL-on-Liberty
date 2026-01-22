package com.example.kafka;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Kafka Producer Service with SASL Plain Text Authentication
 */
@ApplicationScoped
public class KafkaProducerService {

    private static final Logger LOGGER = Logger.getLogger(KafkaProducerService.class.getName());

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
    @ConfigProperty(name = "kafka.producer.key.serializer")
    private String keySerializer;

    @Inject
    @ConfigProperty(name = "kafka.producer.value.serializer")
    private String valueSerializer;

    @Inject
    @ConfigProperty(name = "kafka.producer.acks")
    private String acks;

    @Inject
    @ConfigProperty(name = "kafka.producer.retries")
    private int retries;

    @Inject
    @ConfigProperty(name = "kafka.producer.batch.size")
    private int batchSize;

    @Inject
    @ConfigProperty(name = "kafka.producer.linger.ms")
    private int lingerMs;

    @Inject
    @ConfigProperty(name = "kafka.producer.buffer.memory")
    private long bufferMemory;

    @Inject
    @ConfigProperty(name = "kafka.topic.name")
    private String topicName;

    private Producer<String, String> producer;

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing Kafka Producer with SASL authentication");
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // SASL Configuration
        props.put("security.protocol", securityProtocol);
        props.put("sasl.mechanism", saslMechanism);
        props.put("sasl.jaas.config", saslJaasConfig);

        try {
            producer = new KafkaProducer<>(props);
            LOGGER.info("Kafka Producer initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize Kafka Producer: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Kafka Producer", e);
        }
    }

    /**
     * Send a message to the default topic
     */
    public Future<RecordMetadata> sendMessage(String message) {
        return sendMessage(null, message);
    }

    /**
     * Send a message with a key to the default topic
     */
    public Future<RecordMetadata> sendMessage(String key, String message) {
        return sendMessage(topicName, key, message);
    }

    /**
     * Send a message to a specific topic
     */
    public Future<RecordMetadata> sendMessage(String topic, String key, String message) {
        if (producer == null) {
            throw new IllegalStateException("Producer is not initialized");
        }

        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            
            LOGGER.info(String.format("Sending message to topic '%s' with key '%s': %s", 
                                    topic, key, message));
            
            Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    LOGGER.severe("Failed to send message: " + exception.getMessage());
                } else {
                    LOGGER.info(String.format("Message sent successfully to topic '%s', partition %d, offset %d",
                                             metadata.topic(), metadata.partition(), metadata.offset()));
                }
            });

            return future;
        } catch (Exception e) {
            LOGGER.severe("Error sending message: " + e.getMessage());
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Send a batch of messages
     */
    public void sendMessages(String[] messages) {
        for (int i = 0; i < messages.length; i++) {
            sendMessage("batch-" + i, messages[i]);
        }
        flush();
    }

    /**
     * Flush any pending messages
     */
    public void flush() {
        if (producer != null) {
            producer.flush();
            LOGGER.info("Producer flushed");
        }
    }

    /**
     * Get producer metrics
     */
    public String getMetrics() {
        if (producer != null) {
            return producer.metrics().toString();
        }
        return "Producer not initialized";
    }

    @PreDestroy
    public void cleanup() {
        if (producer != null) {
            LOGGER.info("Closing Kafka Producer");
            producer.close();
        }
    }
}