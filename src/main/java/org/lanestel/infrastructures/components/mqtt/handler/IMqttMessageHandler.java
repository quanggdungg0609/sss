package org.lanestel.infrastructures.components.mqtt.handler;

import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;

public abstract class IMqttMessageHandler {
    @Inject
    Logger log;
    
    public abstract String getHandledTopicType();
    public abstract Uni<Void> handle(MqttMessage<byte[]> message); 

    protected String getClientId(MqttMessage<byte[]> message){
        String topic = message.getTopic();
        String[] topicParts = topic.split("/");
        if (topicParts.length < 2) {
            log.warn("Invalid topic format, cannot extract clientId: " + topic);
            return "";
        }
        return topicParts[1];
    }
}
