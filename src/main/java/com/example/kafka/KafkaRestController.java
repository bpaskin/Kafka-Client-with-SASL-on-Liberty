package com.example.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.RecordMetadata;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST Controller for Kafka Producer and Consumer operations
 */
@Path("/kafka")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class KafkaRestController {

    private static final Logger LOGGER = Logger.getLogger(KafkaRestController.class.getName());

    @Inject
    private KafkaProducerService producerService;

    @Inject
    private KafkaConsumerService consumerService;

    /**
     * Send a simple message to the default topic
     */
    @POST
    @Path("/send")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendMessage(String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message sent successfully");
            response.put("topic", metadata.topic());
            response.put("partition", metadata.partition());
            response.put("offset", metadata.offset());
            response.put("timestamp", metadata.timestamp());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Send a simple message to the default topic (form-encoded)
     */
    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendMessageForm(@FormParam("message") String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            return Response.ok("Message sent successfully to topic: " + metadata.topic() +
                             ", partition: " + metadata.partition() +
                             ", offset: " + metadata.offset()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to send message: " + e.getMessage()).build();
        }
    }

    /**
     * Send a message with a key to the default topic
     */
    @POST
    @Path("/send/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendMessageWithKey(@PathParam("key") String key, String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(key, message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message sent successfully");
            response.put("key", key);
            response.put("topic", metadata.topic());
            response.put("partition", metadata.partition());
            response.put("offset", metadata.offset());
            response.put("timestamp", metadata.timestamp());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message with key: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Send a message to a specific topic
     */
    @POST
    @Path("/send/{topic}/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendMessageToTopic(@PathParam("topic") String topic, 
                                     @PathParam("key") String key, 
                                     String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(topic, key, message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Message sent successfully");
            response.put("key", key);
            response.put("topic", metadata.topic());
            response.put("partition", metadata.partition());
            response.put("offset", metadata.offset());
            response.put("timestamp", metadata.timestamp());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message to topic: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send message: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Send a message with key and message (form-encoded)
     */
    @POST
    @Path("/send/key-message")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendMessageWithKeyForm(@FormParam("key") String key, @FormParam("message") String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(key, message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            return Response.ok("Message with key sent successfully to topic: " + metadata.topic() +
                             ", partition: " + metadata.partition() +
                             ", offset: " + metadata.offset()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message with key: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to send message with key: " + e.getMessage()).build();
        }
    }

    /**
     * Send a message to specific topic with key and message (form-encoded)
     */
    @POST
    @Path("/send/topic-key-message")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response sendMessageToTopicForm(@FormParam("topic") String topic,
                                          @FormParam("key") String key,
                                          @FormParam("message") String message) {
        try {
            Future<RecordMetadata> future = producerService.sendMessage(topic, key, message);
            RecordMetadata metadata = future.get(); // Wait for completion
            
            return Response.ok("Message sent to topic successfully: " + metadata.topic() +
                             ", partition: " + metadata.partition() +
                             ", offset: " + metadata.offset()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send message to topic: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to send message to topic: " + e.getMessage()).build();
        }
    }

    /**
     * Send multiple messages as a batch
     */
    @POST
    @Path("/send/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendBatchMessages(BatchMessageRequest request) {
        try {
            String[] messageArray = request.getMessages().toArray(new String[0]);
            producerService.sendMessages(messageArray);
            
            return Response.ok("Batch messages sent successfully. Count: " + request.getMessages().size()).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to send batch messages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to send batch messages: " + e.getMessage()).build();
        }
    }

    /**
     * Inner class to handle batch message requests
     */
    public static class BatchMessageRequest {
        private List<String> messages;
        
        public List<String> getMessages() {
            return messages;
        }
        
        public void setMessages(List<String> messages) {
            this.messages = messages;
        }
    }

    /**
     * Start consuming messages from the default topic
     */
    @POST
    @Path("/consume/start")
    public Response startConsuming() {
        try {
            consumerService.startConsuming();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Consumer started successfully");
            response.put("running", consumerService.isRunning());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to start consumer: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to start consumer: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Start consuming messages from a specific topic
     */
    @POST
    @Path("/consume/start/{topic}")
    public Response startConsumingTopic(@PathParam("topic") String topic) {
        try {
            consumerService.startConsuming(topic);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Consumer started successfully for topic: " + topic);
            response.put("topic", topic);
            response.put("running", consumerService.isRunning());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to start consumer for topic: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to start consumer: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Stop consuming messages
     */
    @POST
    @Path("/consume/stop")
    public Response stopConsuming() {
        try {
            consumerService.stopConsuming();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Consumer stopped successfully");
            response.put("running", consumerService.isRunning());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to stop consumer: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to stop consumer: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Get consumed messages
     */
    @GET
    @Path("/consume/messages")
    public Response getConsumedMessages() {
        try {
            List<String> messages = consumerService.getConsumedMessages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("running", consumerService.isRunning());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get consumed messages: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get consumed messages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Clear consumed messages
     */
    @DELETE
    @Path("/consume/messages")
    public Response clearConsumedMessages() {
        try {
            consumerService.clearConsumedMessages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Consumed messages cleared successfully");
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to clear consumed messages: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to clear consumed messages: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Get consumer status and information
     */
    @GET
    @Path("/consume/status")
    public Response getConsumerStatus() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("running", consumerService.isRunning());
            response.put("groupInfo", consumerService.getConsumerGroupInfo());
            response.put("messageCount", consumerService.getConsumedMessages().size());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get consumer status: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get consumer status: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Get producer metrics
     */
    @GET
    @Path("/producer/metrics")
    public Response getProducerMetrics() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("metrics", producerService.getMetrics());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get producer metrics: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get producer metrics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Get consumer metrics
     */
    @GET
    @Path("/consumer/metrics")
    public Response getConsumerMetrics() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("metrics", consumerService.getMetrics());
            
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.severe("Failed to get consumer metrics: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to get consumer metrics: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GET
    @Path("/health")
    public Response healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Kafka SASL Demo");
        response.put("consumerRunning", consumerService.isRunning());
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }
}