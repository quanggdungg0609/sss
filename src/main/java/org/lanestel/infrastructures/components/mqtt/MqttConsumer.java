package org.lanestel.infrastructures.components.mqtt;

import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.mqtt.dispatcher.MqttMessageDispatcher;
import org.lanestel.infrastructures.components.mqtt.handler.IMqttMessageHandler;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * MQTT Consumer responsible for processing incoming MQTT messages.
 * Each message is dispatched to appropriate handlers based on topic type.
 */
@ApplicationScoped
public class MqttConsumer {

    private final Logger log;
    private final MqttMessageDispatcher dispatcher;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param log Logger for debugging and monitoring
     * @param dispatcher Message dispatcher to route messages to appropriate handlers
     */
    @Inject
    public MqttConsumer(Logger log, MqttMessageDispatcher dispatcher) {
        this.log = log;
        this.dispatcher = dispatcher;
    }

    /**
     * Processes incoming MQTT messages with proper session management.
     * Each message processing gets its own Hibernate Reactive session.
     * 
     * @param message The incoming MQTT message
     * @return Uni<Void> representing the completion of message processing
     */
    @Incoming("sensors-in")
    public Uni<Void> process(MqttMessage<byte[]> message) {
        String topic = message.getTopic();
        String topicType = getLastSegmentFromTopic(topic);
        
        log.info("Processing MQTT message for topic: " + topic + ", type: " + topicType);

        IMqttMessageHandler handler = dispatcher.getHandler(topicType);

        if (handler != null) {
            // Wrap handler execution to ensure proper error handling and session management
            return handler.handle(message)
                .onItem().invoke(() -> {
                    log.info("Successfully processed message for topic: " + topic);
                })
                .onFailure().invoke(throwable -> {
                    log.error("Failed to process message for topic: " + topic, throwable);
                })
                .onFailure().recoverWithItem(() -> {
                    // Recover from failures to prevent message processing from stopping
                    log.warn("Recovered from failure for topic: " + topic);
                    return null;
                });
        } else {
            log.warn("No handler found for topic type: " + topicType);
            return Uni.createFrom().voidItem();
        }
    }

    /**
     * Extracts the last segment from MQTT topic to determine message type.
     * 
     * @param topic The full MQTT topic string
     * @return The last segment of the topic (message type)
     */
    private String getLastSegmentFromTopic(String topic) {
        String[] parts = topic.split("/");
        return parts[parts.length - 1];
    }
}