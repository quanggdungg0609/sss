package org.lanestel.infrastructures.components.mqtt;

import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.mqtt.MqttMessage;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;


@ApplicationScoped
public class MqttConsumer {

    @Inject
    Logger log;

    // Annotation này yêu cầu framework lắng nghe kênh "sensors-in"
    // Framework sẽ tự động tìm cấu hình trong .properties và kết nối
    @Incoming("sensors-in")
    public Uni<Void> process(MqttMessage<byte[]> message) {
        String topic = message.getTopic();
        String payload = new String(message.getPayload());
        log.info("Received telemetry from topic '" + topic + "': " + payload);
        return Uni.createFrom().voidItem();

    }
}