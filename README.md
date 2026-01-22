# Kafka SASL Demo - Jakarta EE 10

A comprehensive demonstration application showcasing **Apache Kafka** producer and consumer functionality with **SASL Plain Text authentication** using **Jakarta EE 10** and **Open Liberty**.

## 🎯 Purpose

This application demonstrates:
- **Kafka Producer/Consumer**: Complete implementation with SASL authentication
- **Jakarta EE 10**: Modern enterprise Java development
- **SASL Plain Text**: Secure authentication mechanism for Kafka
- **RESTful APIs**: JAX-RS 3.1 endpoints for Kafka operations
- **MicroProfile Config**: Configuration management
- **CDI 4.0**: Dependency injection and lifecycle management

## 🏗️ Architecture

The application uses the following technology stack:

- **[Jakarta EE 10](https://jakarta.ee/)** - Enterprise Java platform
- **[Open Liberty](https://openliberty.io/)** - Lightweight application server
- **[Apache Kafka](https://kafka.apache.org/)** - Distributed streaming platform
- **[SASL Plain Text](https://kafka.apache.org/documentation/#security_sasl_plain)** - Authentication mechanism
- **[MicroProfile 6.0](https://microprofile.io/)** - Cloud-native Java specifications
- **[Maven](https://maven.apache.org/)** - Build and dependency management
- **Java 21** - Runtime environment

## 📁 Project Structure

```
kafka_sasl/
├── pom.xml                                           # Maven build configuration
├── src/main/
│   ├── java/com/example/kafka/
│   │   ├── KafkaApplication.java                     # JAX-RS Application
│   │   ├── KafkaProducerService.java                 # Kafka producer service
│   │   ├── KafkaConsumerService.java                 # Kafka consumer service
│   │   ├── KafkaRestController.java                  # REST endpoints
│   │   └── CorsFilter.java                           # CORS filter
│   ├── liberty/config/
│   │   ├── server.xml                                # Liberty server configuration
│   │   ├── kafka_client_jaas.conf                    # JAAS configuration
│   │   └── jvm.options                               # JVM options
│   ├── resources/META-INF/
│   │   └── microprofile-config.properties            # Application configuration
│   └── webapp/
│       ├── index.jsp                                 # Main testing interface
│       └── WEB-INF/
│           └── web.xml                               # Web application configuration
```

## 🚀 Features

### Kafka Producer
- **Send messages** to default or custom topics
- **Message keys** for partitioning
- **Batch messaging** support
- **Asynchronous operations** with callbacks
- **Configurable serialization** (String by default)

### Kafka Consumer
- **Subscribe to topics** with automatic offset management
- **Consumer groups** for scalable processing
- **Real-time message consumption** with polling
- **Message storage** for web interface display
- **Start/stop controls** for consumer lifecycle

### REST API
- **Complete CRUD operations** for producer/consumer
- **JSON responses** with detailed metadata
- **Error handling** with proper HTTP status codes
- **CORS support** for web interface
- **Health checks** and metrics endpoints

### Web Interface
- **Interactive testing** of producer/consumer operations
- **Real-time message display** with auto-refresh
- **Configuration management** through UI
- **Status monitoring** and metrics visualization
- **Responsive design** for mobile and desktop

## 🛠️ Prerequisites

1. **Java 21** or higher
2. **Maven 3.8+**
3. **Apache Kafka** with SASL Plain Text authentication
4. **Zookeeper** (if using Kafka < 2.8)

### Installing and Configuring Kafka with SASL

#### 1. Download and Install Kafka
```bash
# Download Kafka
wget https://downloads.apache.org/kafka/2.13-3.6.1/kafka_2.13-3.6.1.tgz
tar -xzf kafka_2.13-3.6.1.tgz
cd kafka_2.13-3.6.1
```

#### 2. Configure SASL Plain Text Authentication

Create `config/kafka_server_jaas.conf`:
```
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_kafka-user="kafka-password";
};
```

Update `config/server.properties`:
```properties
# SASL Configuration
listeners=SASL_PLAINTEXT://localhost:9092
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN

# Authentication
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
super.users=User:admin
```

#### 3. Start Kafka with SASL
```bash
# Set JAAS configuration
export KAFKA_OPTS="-Djava.security.auth.login.config=/path/to/kafka_server_jaas.conf"

# Start Zookeeper (if needed)
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 4. Create Topic
```bash
bin/kafka-topics.sh --create --topic demo-topic --bootstrap-server localhost:9092 \
  --partitions 3 --replication-factor 1 \
  --command-config config/client-sasl.properties
```

Create `config/client-sasl.properties`:
```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka-user" password="kafka-password";
```

## 🏃‍♂️ Quick Start

1. **Clone and navigate to the project:**
   ```bash
   cd kafka_sasl
   ```

2. **Configure the application:**
   Edit [`microprofile-config.properties`](src/main/resources/META-INF/microprofile-config.properties) if needed:
   ```properties
   kafka.bootstrap.servers=localhost:9092
   kafka.security.protocol=SASL_PLAINTEXT
   kafka.sasl.mechanism=PLAIN
   kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka-user" password="kafka-password";
   ```

3. **Build and run the application:**
   ```bash
   mvn clean package liberty:run
   ```

4. **Access the application:**
   - Web Interface: http://localhost:9080/kafka-sasl
   - REST API: http://localhost:9080/kafka-sasl/api/kafka/health

## 🧪 Testing the Application

### Using the Web Interface

1. **Open your browser** to: http://localhost:9080/kafka-sasl
2. **Send messages** using the producer section
3. **Start the consumer** to receive messages
4. **Monitor real-time** message consumption

### Using REST API

#### Send a Message
```bash
curl -X POST http://localhost:9080/kafka-sasl/api/kafka/send \
  -H "Content-Type: text/plain" \
  -d "Hello Kafka with SASL!"
```

#### Send Message with Key
```bash
curl -X POST http://localhost:9080/kafka-sasl/api/kafka/send/my-key \
  -H "Content-Type: text/plain" \
  -d "Message with key"
```

#### Start Consumer
```bash
curl -X POST http://localhost:9080/kafka-sasl/api/kafka/consume/start
```

#### Get Consumed Messages
```bash
curl http://localhost:9080/kafka-sasl/api/kafka/consume/messages
```

#### Stop Consumer
```bash
curl -X POST http://localhost:9080/kafka-sasl/api/kafka/consume/stop
```

## 📊 REST API Reference

### Producer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/kafka/send` | Send simple message |
| POST | `/api/kafka/send/{key}` | Send message with key |
| POST | `/api/kafka/send/{topic}/{key}` | Send to specific topic |
| POST | `/api/kafka/send/batch` | Send batch messages |

### Consumer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/kafka/consume/start` | Start consumer |
| POST | `/api/kafka/consume/start/{topic}` | Start consumer for topic |
| POST | `/api/kafka/consume/stop` | Stop consumer |
| GET | `/api/kafka/consume/messages` | Get consumed messages |
| DELETE | `/api/kafka/consume/messages` | Clear messages |
| GET | `/api/kafka/consume/status` | Get consumer status |

### Monitoring Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/kafka/health` | Health check |
| GET | `/api/kafka/producer/metrics` | Producer metrics |
| GET | `/api/kafka/consumer/metrics` | Consumer metrics |

## ⚙️ Configuration

### Application Configuration ([`microprofile-config.properties`](src/main/resources/META-INF/microprofile-config.properties))

```properties
# Kafka Connection
kafka.bootstrap.servers=localhost:9092
kafka.security.protocol=SASL_PLAINTEXT
kafka.sasl.mechanism=PLAIN
kafka.sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="kafka-user" password="kafka-password";

# Producer Settings
kafka.producer.acks=all
kafka.producer.retries=3
kafka.producer.batch.size=16384

# Consumer Settings
kafka.consumer.group.id=kafka-sasl-demo-group
kafka.consumer.auto.offset.reset=earliest
kafka.consumer.enable.auto.commit=true

# Topic Configuration
kafka.topic.name=demo-topic
kafka.topic.partitions=3
kafka.topic.replication.factor=1
```

### SASL Configuration ([`kafka_client_jaas.conf`](src/main/liberty/config/kafka_client_jaas.conf))

```
KafkaClient {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="kafka-user"
    password="kafka-password";
};
```

### Liberty Server Configuration ([`server.xml`](src/main/liberty/config/server.xml))

Key features enabled:
- `jakartaee-10.0` - Full Jakarta EE platform
- `microProfile-6.0` - MicroProfile specifications
- `restfulWS-3.1` - JAX-RS REST services
- `cdi-4.0` - Contexts and Dependency Injection

## 🔧 Development

### Building
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Packaging
```bash
mvn package
```

### Development Mode (Hot Reload)
```bash
mvn liberty:dev
```

### Debugging
```bash
mvn liberty:debug
```

## 🐛 Troubleshooting

### Common Issues

**SASL Authentication Failed:**
- Verify Kafka server SASL configuration
- Check username/password in JAAS configuration
- Ensure Kafka is started with JAAS config: `KAFKA_OPTS="-Djava.security.auth.login.config=..."`

**Connection Refused:**
- Verify Kafka is running on localhost:9092
- Check firewall settings
- Ensure SASL_PLAINTEXT listener is configured

**Topic Not Found:**
- Create topic manually: `kafka-topics.sh --create --topic demo-topic ...`
- Check topic permissions for SASL user
- Verify topic name in configuration

**Consumer Not Receiving Messages:**
- Check consumer group configuration
- Verify topic subscription
- Check offset reset policy (`earliest` vs `latest`)

**Liberty Server Issues:**
- Check JVM options are properly set
- Verify Kafka client JARs are in lib directory
- Review Liberty logs for detailed error messages

### Logging

Enable detailed logging by modifying [`server.xml`](src/main/liberty/config/server.xml):
```xml
<logging traceSpecification="*=info:com.example.kafka.*=all:org.apache.kafka.*=debug"/>
```

### Monitoring

Access Liberty metrics:
- Admin Center: https://localhost:9443/adminCenter
- Metrics: http://localhost:9080/kafka-sasl/api/kafka/health

## 📚 Key Components

### [`KafkaProducerService.java`](src/main/java/com/example/kafka/KafkaProducerService.java)
CDI service managing Kafka producer lifecycle with SASL authentication, message sending, and metrics.

### [`KafkaConsumerService.java`](src/main/java/com/example/kafka/KafkaConsumerService.java)
CDI service handling Kafka consumer operations with background polling, message storage, and lifecycle management.

### [`KafkaRestController.java`](src/main/java/com/example/kafka/KafkaRestController.java)
JAX-RS REST endpoints providing HTTP API for all producer/consumer operations with JSON responses.

### [`index.jsp`](src/main/webapp/index.jsp)
Interactive web interface for testing Kafka operations with real-time updates and responsive design.

## 🔒 Security Considerations

- **SASL Plain Text**: Credentials are transmitted in plain text over the network
- **Production Use**: Consider SASL_SSL for encrypted communication
- **Credential Management**: Use environment variables or secure vaults for passwords
- **Network Security**: Implement proper firewall rules and network segmentation

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is provided as a demonstration and learning resource.

---

**Note:** This is a demonstration application. For production use, implement proper security measures, monitoring, error handling, and consider using SASL_SSL for encrypted communication.