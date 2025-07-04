package org.lanestel.infrastructures.components.mqtt.dispatcher;


import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.lanestel.infrastructures.components.mqtt.handler.IMqttMessageHandler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;


@ApplicationScoped
public class MqttMessageDispatcher {

    @Inject
    Instance<IMqttMessageHandler> handlers;

    private Map<String, IMqttMessageHandler> handlerMap;

    @PostConstruct
    void init() {
        
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(IMqttMessageHandler::getHandledTopicType, Function.identity()));
    }

    public IMqttMessageHandler getHandler(String topicType) {
        return handlerMap.get(topicType);
    }
}

