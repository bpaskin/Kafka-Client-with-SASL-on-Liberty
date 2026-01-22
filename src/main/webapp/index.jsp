<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kafka SASL Application</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 1000px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            color: #333;
            text-align: center;
            margin-bottom: 30px;
        }
        .section {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 8px;
        }
        .section h2 {
            color: #555;
            margin-top: 0;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"], textarea {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
        }
        textarea {
            height: 100px;
            resize: vertical;
        }
        button {
            background-color: #007bff;
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            margin-right: 10px;
            margin-bottom: 10px;
        }
        button:hover {
            background-color: #0056b3;
        }
        button.secondary {
            background-color: #6c757d;
        }
        button.secondary:hover {
            background-color: #545b62;
        }
        button.danger {
            background-color: #dc3545;
        }
        button.danger:hover {
            background-color: #c82333;
        }
        .response {
            margin-top: 20px;
            padding: 15px;
            border-radius: 4px;
            display: none;
        }
        .success {
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            color: #155724;
        }
        .error {
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            color: #721c24;
        }
        .info {
            background-color: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
        }
        .status-panel {
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .messages-display {
            max-height: 300px;
            overflow-y: auto;
            border: 1px solid #ddd;
            padding: 10px;
            background-color: #f8f9fa;
            border-radius: 4px;
            margin-top: 10px;
        }
        .message-item {
            padding: 5px 0;
            border-bottom: 1px solid #eee;
        }
        .message-item:last-child {
            border-bottom: none;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Kafka SASL Producer/Consumer Dashboard</h1>
        
        <!-- Status Panel -->
        <div class="status-panel">
            <h3>System Status</h3>
            <div id="status-info">Loading...</div>
            <button onclick="refreshStatus()">Refresh Status</button>
        </div>
        
        <!-- Producer Section -->
        <div class="section">
            <h2>Message Producer</h2>
            
            <div class="form-group">
                <label for="producer-topic">Topic:</label>
                <input type="text" id="producer-topic" value="test-topic" placeholder="Enter Kafka topic name">
            </div>
            
            <div class="form-group">
                <label for="producer-key">Key (optional):</label>
                <input type="text" id="producer-key" placeholder="Enter message key">
            </div>
            
            <div class="form-group">
                <label for="producer-message">Message:</label>
                <textarea id="producer-message" placeholder="Enter your message here"></textarea>
            </div>
            
            <button onclick="sendMessage()">Send Message</button>
            <button onclick="sendMessageWithKey()" class="secondary">Send with Key</button>
            <button onclick="sendMessageToTopic()" class="secondary">Send to Specific Topic</button>
            
            <div class="form-group">
                <label for="batch-messages">Batch Messages (one per line):</label>
                <textarea id="batch-messages" placeholder="Enter multiple messages, one per line"></textarea>
                <button onclick="sendBatchMessages()" class="secondary">Send Batch</button>
            </div>
        </div>
        
        <!-- Consumer Section -->
        <div class="section">
            <h2>Message Consumer</h2>
            
            <div class="form-group">
                <label for="consumer-topic">Topic:</label>
                <input type="text" id="consumer-topic" value="test-topic" placeholder="Enter Kafka topic name">
            </div>
            
            <button onclick="startConsumer()">Start Consumer</button>
            <button onclick="startConsumerForTopic()" class="secondary">Start for Specific Topic</button>
            <button onclick="stopConsumer()" class="danger">Stop Consumer</button>
            <button onclick="getConsumedMessages()" class="secondary">Get Messages</button>
            <button onclick="clearMessages()" class="danger">Clear Messages</button>
            
            <div class="messages-display" id="consumed-messages">
                <p>No messages consumed yet. Start the consumer to see messages here.</p>
            </div>
        </div>
        
        <!-- Metrics Section -->
        <div class="section">
            <h2>Metrics & Monitoring</h2>
            <button onclick="getProducerMetrics()" class="secondary">Producer Metrics</button>
            <button onclick="getConsumerMetrics()" class="secondary">Consumer Metrics</button>
            <button onclick="getConsumerStatus()" class="secondary">Consumer Status</button>
        </div>
        
        <div id="response" class="response"></div>
    </div>

    <script>
        // Initialize page
        document.addEventListener('DOMContentLoaded', function() {
            refreshStatus();
            // Auto-refresh status every 30 seconds
            setInterval(refreshStatus, 30000);
        });

        function refreshStatus() {
            fetch('api/kafka/health')
            .then(response => response.json())
            .then(data => {
                const statusDiv = document.getElementById('status-info');
                statusDiv.innerHTML =
                    '<strong>Service:</strong> ' + (data.service || 'Kafka SASL Demo') + '<br>' +
                    '<strong>Status:</strong> ' + (data.status || 'Unknown') + '<br>' +
                    '<strong>Consumer Running:</strong> ' + (data.consumerRunning ? 'Yes' : 'No') + '<br>' +
                    '<strong>Last Updated:</strong> ' + new Date().toLocaleString();
            })
            .catch(error => {
                document.getElementById('status-info').innerHTML = 'Error loading status: ' + error.message;
            });
        }

        function sendMessage() {
            const message = document.getElementById('producer-message').value;
            
            if (!message) {
                showResponse('Please enter a message', 'error');
                return;
            }
            
            // Use encodeURIComponent to properly encode the message for URL transmission
            const encodedMessage = encodeURIComponent(message);
            
            fetch('api/kafka/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'message=' + encodedMessage
            })
            .then(response => response.text())
            .then(data => {
                showResponse('Message sent successfully: ' + data, 'success');
                document.getElementById('producer-message').value = '';
            })
            .catch(error => {
                showResponse('Error sending message: ' + error, 'error');
            });
        }

        function sendMessageWithKey() {
            const key = document.getElementById('producer-key').value;
            const message = document.getElementById('producer-message').value;
            
            if (!key || !message) {
                showResponse('Please enter both key and message', 'error');
                return;
            }
            
            // Use encodeURIComponent to properly encode parameters
            const encodedKey = encodeURIComponent(key);
            const encodedMessage = encodeURIComponent(message);
            
            fetch('api/kafka/send/key-message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'key=' + encodedKey + '&message=' + encodedMessage
            })
            .then(response => response.text())
            .then(data => {
                showResponse('Message with key sent successfully: ' + data, 'success');
                document.getElementById('producer-key').value = '';
                document.getElementById('producer-message').value = '';
            })
            .catch(error => {
                showResponse('Error sending message with key: ' + error, 'error');
            });
        }

        function sendMessageToTopic() {
            const topic = document.getElementById('producer-topic').value;
            const key = document.getElementById('producer-key').value;
            const message = document.getElementById('producer-message').value;
            
            if (!topic || !message) {
                showResponse('Please enter both topic and message', 'error');
                return;
            }
            
            // Use encodeURIComponent to properly encode parameters
            const encodedTopic = encodeURIComponent(topic);
            const encodedKey = encodeURIComponent(key || '');
            const encodedMessage = encodeURIComponent(message);
            
            fetch('api/kafka/send/topic-key-message', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'topic=' + encodedTopic + '&key=' + encodedKey + '&message=' + encodedMessage
            })
            .then(response => response.text())
            .then(data => {
                showResponse('Message sent to topic successfully: ' + data, 'success');
                document.getElementById('producer-message').value = '';
                if (key) document.getElementById('producer-key').value = '';
            })
            .catch(error => {
                showResponse('Error sending message to topic: ' + error, 'error');
            });
        }

        function sendBatchMessages() {
            const batchText = document.getElementById('batch-messages').value;
            
            if (!batchText.trim()) {
                showResponse('Please enter batch messages', 'error');
                return;
            }
            
            const messages = batchText.split('\n').filter(msg => msg.trim());
            
            fetch('api/kafka/send/batch', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ messages: messages })
            })
            .then(response => response.text())
            .then(data => {
                showResponse('Batch messages sent successfully: ' + data, 'success');
                document.getElementById('batch-messages').value = '';
            })
            .catch(error => {
                showResponse('Error sending batch messages: ' + error, 'error');
            });
        }

        function startConsumer() {
            fetch('api/kafka/consume/start', {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                showResponse('Consumer started: ' + data.message, 'success');
                refreshStatus();
            })
            .catch(error => {
                showResponse('Error starting consumer: ' + error, 'error');
            });
        }

        function startConsumerForTopic() {
            const topic = document.getElementById('consumer-topic').value;
            
            if (!topic) {
                showResponse('Please enter a topic name', 'error');
                return;
            }
            
            // Use encodeURIComponent to properly encode the topic
            const encodedTopic = encodeURIComponent(topic);
            
            fetch('api/kafka/consume/start/' + encodedTopic, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                showResponse('Consumer started for topic ' + topic + ': ' + data.message, 'success');
                refreshStatus();
            })
            .catch(error => {
                showResponse('Error starting consumer for topic: ' + error, 'error');
            });
        }

        function stopConsumer() {
            fetch('api/kafka/consume/stop', {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                showResponse('Consumer stopped: ' + data.message, 'success');
                refreshStatus();
            })
            .catch(error => {
                showResponse('Error stopping consumer: ' + error, 'error');
            });
        }

        function getConsumedMessages() {
            fetch('api/kafka/consume/messages')
            .then(response => response.json())
            .then(data => {
                const messagesDiv = document.getElementById('consumed-messages');
                if (data.messages && data.messages.length > 0) {
                    messagesDiv.innerHTML = '<h4>Consumed Messages (' + data.count + '):</h4>';
                    data.messages.forEach((message, index) => {
                        const messageDiv = document.createElement('div');
                        messageDiv.className = 'message-item';
                        messageDiv.textContent = (index + 1) + '. ' + message;
                        messagesDiv.appendChild(messageDiv);
                    });
                } else {
                    messagesDiv.innerHTML = '<p>No messages consumed yet.</p>';
                }
                showResponse('Retrieved ' + (data.count || 0) + ' consumed messages', 'info');
            })
            .catch(error => {
                showResponse('Error getting consumed messages: ' + error, 'error');
            });
        }

        function clearMessages() {
            fetch('api/kafka/consume/messages', {
                method: 'DELETE'
            })
            .then(response => response.json())
            .then(data => {
                document.getElementById('consumed-messages').innerHTML = '<p>Messages cleared.</p>';
                showResponse('Consumed messages cleared: ' + data.message, 'success');
            })
            .catch(error => {
                showResponse('Error clearing messages: ' + error, 'error');
            });
        }

        function getProducerMetrics() {
            fetch('api/kafka/producer/metrics')
            .then(response => response.json())
            .then(data => {
                showResponse('Producer metrics retrieved (check console for details)', 'info');
                console.log('Producer Metrics:', data.metrics);
            })
            .catch(error => {
                showResponse('Error getting producer metrics: ' + error, 'error');
            });
        }

        function getConsumerMetrics() {
            fetch('api/kafka/consumer/metrics')
            .then(response => response.json())
            .then(data => {
                showResponse('Consumer metrics retrieved (check console for details)', 'info');
                console.log('Consumer Metrics:', data.metrics);
            })
            .catch(error => {
                showResponse('Error getting consumer metrics: ' + error, 'error');
            });
        }

        function getConsumerStatus() {
            fetch('api/kafka/consume/status')
            .then(response => response.json())
            .then(data => {
                const statusInfo = 'Running: ' + data.running + ', Group Info: ' + data.groupInfo + ', Messages: ' + data.messageCount;
                showResponse('Consumer Status: ' + statusInfo, 'info');
            })
            .catch(error => {
                showResponse('Error getting consumer status: ' + error, 'error');
            });
        }
        
        function showResponse(message, type) {
            const responseDiv = document.getElementById('response');
            responseDiv.textContent = message;
            responseDiv.className = 'response ' + type;
            responseDiv.style.display = 'block';
            
            // Hide the response after 8 seconds
            setTimeout(() => {
                responseDiv.style.display = 'none';
            }, 8000);
        }
    </script>
</body>
</html>