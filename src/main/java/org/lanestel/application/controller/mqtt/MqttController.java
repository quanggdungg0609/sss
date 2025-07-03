package org.lanestel.application.controller.mqtt;

import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;
import org.lanestel.application.dtos.mqtt.request.EmqxAclRequest;
import org.lanestel.application.dtos.mqtt.request.EmqxAuthRequest;
import org.lanestel.application.service.mqtt.MqttService;

import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for MQTT authentication and authorization endpoints.
 * Handles HTTP requests from EMQX broker for client authentication and ACL checks.
 */
@Path("/api/v1/mqtt")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MqttController {
    private final Logger log;
    private final MqttService mqttService;

    /**
     * Constructor with dependency injection
     * @param log Logger for debugging and monitoring
     * @param mqttService Service for MQTT operations
     */
    @Inject
    public MqttController(
        Logger log,
        MqttService mqttService
    ){
        this.log = log;
        this.mqttService = mqttService;
    }

    /**
     * Handles MQTT client authentication requests from EMQX HTTP authentication
     * @param request The authentication request containing client credentials
     * @return Response with authentication result for EMQX
     */
    @POST
    @Path("/auth")
    @WithSession
    public Uni<Response> authentication(
            @NotNull(message = "Request body is required") @Valid EmqxAuthRequest request
    ) {
        log.info("Received authentication request for username: " + request.getUsername() + 
                ", clientId: " + request.getClientId());
        return mqttService.authenticate(request)
            .onItem().invoke(response -> {
                log.info("Authentication response: " + response.getEntity());
            });
    }
    
    /**
     * Handles MQTT client authorization requests from EMQX HTTP ACL
     * @param request The authorization request containing topic and action information
     * @return Response with authorization result for EMQX
     */
    @POST
    @Path("/acl")
    @WithSession
    public Uni<Response> authorization(
            @NotNull(message = "Request body is required") @Valid EmqxAclRequest request
    ) {
        log.info("Received authorization request for username: " + request.getUsername() + 
                ", topic: " + request.getTopic() + ", action: " + request.getAction());
        return mqttService.authorize(request)
            .onItem().invoke(response -> {
                log.info("Authorization response: " + response.getEntity());
            });
    }
}
