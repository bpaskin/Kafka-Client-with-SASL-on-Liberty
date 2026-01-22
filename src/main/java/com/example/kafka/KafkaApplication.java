package com.example.kafka;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class to configure REST endpoints
 */
@ApplicationPath("/api")
public class KafkaApplication extends Application {
    // The @ApplicationPath annotation defines the base URI for all REST endpoints
    // All REST endpoints will be available under /kafka-sasl/api/
}