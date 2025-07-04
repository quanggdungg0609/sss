package org.lanestel.infrastructures.components.mqtt.handler;

import org.jboss.logging.Logger;

import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class StatusMessageHandler extends IMqttMessageHandler {

    @Inject
    Logger log;
    
    @Override
    public String getHandledTopicType() {
        return "status"; // Handler này dành cho "status"
    }

    @Override
    public Uni<Void> handle(MqttMessage<byte[]> message) {
        log.info("Executing Status logic for topic: " + message.getTopic());
       
        return Uni.createFrom().voidItem();
    }
}