package org.lanestel.infrastructures.components.mqtt;


import io.quarkus.runtime.Startup;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Startup
@ApplicationScoped
public class MqttConsumer {
    private final Logger log;

    @Inject
    public MqttConsumer(Logger log) {
        this.log = log;
    }


    @Incoming("sensors-in")
    public void process(MqttMessage<byte[]> message){
        String topic = message.getTopic();
        String payload = new String(message.getPayload());
        log.info("Received telemetry from topic '" + topic + "': " + payload);
    }
}
