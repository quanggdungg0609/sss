package org.lanestel.infrastructures.components.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * MQTT Consumer service that handles incoming sensor telemetry data
 * using HiveMQ client for MQTT communication
 */
@ApplicationScoped
public class MqttConsumer {
    private final Logger log;
    private Mqtt3AsyncClient mqttClient;
    
    @ConfigProperty(name = "mqtt.broker.host", defaultValue = "localhost")
    String mqttHost;
    
    @ConfigProperty(name = "mqtt.broker.port", defaultValue = "1883")
    int mqttPort;
    
    @ConfigProperty(name = "mqtt.user", defaultValue = "admin")
    String mqttUsername;
    
    @ConfigProperty(name = "mqtt.password", defaultValue = "admin123")
    String mqttPassword;

    @ConfigProperty(name = "mqtt.clientid", defaultValue = "ADMIN_CLIENT")
    String mqttClientId;

    @Inject
    public MqttConsumer(Logger log) {
        this.log = log;
    }

    /**
     * Initialize MQTT client and establish connection on application startup
     * @param event Quarkus startup event
     */
    void onStart(@Observes StartupEvent event) {
        connectToMqtt();
    }

    /**
     * Establishes connection to MQTT broker and subscribes to sensor topics
     */
    private void connectToMqtt() {
        try {
            // Create MQTT client
            mqttClient = MqttClient.builder()
                    .useMqttVersion3()
                    .identifier("sensor-consumer-client")
                    .serverHost(mqttHost)
                    .serverPort(mqttPort)
                    .identifier(mqttClientId)
                    .buildAsync();

            // Connect to broker
            mqttClient.connectWith()
                    .simpleAuth()
                    .username(mqttUsername)
                    .password(mqttPassword.getBytes())
                    .applySimpleAuth()
                    
                    .send()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable != null) {
                            log.error("Failed to connect to MQTT broker", throwable);
                        } else {
                            log.info("Successfully connected to MQTT broker at " + mqttHost + ":" + mqttPort);
                            subscribeToTopics();
                        }
                    });

        } catch (Exception e) {
            log.error("Error initializing MQTT client", e);
        }
    }

    /**
     * Subscribes to sensor telemetry topics and handles incoming messages
     */
    private void subscribeToTopics() {
        mqttClient.subscribeWith()
                .topicFilter("sensor/+/telemetry")
                .callback(publish -> {
                    String topic = publish.getTopic().toString();
                    String payload = new String(publish.getPayloadAsBytes());
                    log.info("Received telemetry from topic '" + topic + "': " + payload);
                    
                    // Process the sensor data here
                    processSensorData(topic, payload);
                })
                .send()
                .whenComplete((subAck, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to subscribe to topics", throwable);
                    } else {
                        log.info("Successfully subscribed to sensors/+/telemetry");
                    }
                });
    }

    /**
     * Processes incoming sensor telemetry data
     * @param topic MQTT topic from which the message was received
     * @param payload Message payload containing sensor data
     */
    private void processSensorData(String topic, String payload) {
        // Add your sensor data processing logic here
        // For example: parse JSON, validate data, store to database, etc.
    }
}
