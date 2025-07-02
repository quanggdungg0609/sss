package org.lanestel.application.controller.mqtt;

import jakarta.ws.rs.Path;

import org.jboss.logging.Logger;
import org.lanestel.application.dtos.mqtt.request.EmqxAclRequest;
import org.lanestel.application.dtos.mqtt.request.EmqxAuthRequest;
import org.lanestel.application.dtos.mqtt.response.EmqxResponse;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/mqtt")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MqttController {
    private final Logger log;

    @Inject
    public MqttController(
        Logger log
    ){
        this.log = log;
    }

    /**
     * Handles MQTT client authentication requests from EMQX HTTP authentication
     * @param request The authentication request containing client credentials
     * @return Response with authentication result for EMQX
     */
    @POST
    @Path("/auth")
    public Uni<Response> authentication(
            @NotNull(message = "Request body is required") @Valid EmqxAuthRequest request) {
        
        log.info("=== EMQX MQTT Auth Request START ===");
        log.info("Request: " + request);
        log.info("Client ID: " + request.getClientId());
        log.info("Username: " + request.getUsername());
        log.info("Password: " + request.getPassword());
        log.info("Peer Host: " + request.getPeerHost());
        log.info("Protocol: " + request.getProtocol());
        
        // TODO: Implement actual authentication logic
        // For now, allow all connections
        EmqxResponse response = EmqxResponse.allow();
        
        log.info("Response: " + response);
        log.info("=== EMQX MQTT Auth Request END ===");
        
        return Uni.createFrom().item(
            Response.ok(response)
                .header("Content-Type", "application/json")
                .build()
        );
    }
    
    /**
     * Handles MQTT ACL (authorization) requests from EMQX HTTP authorization
     * @param request The ACL request containing client and topic information
     * @return Response with authorization result for EMQX
     */
    @POST
    @Path("/acl")
    public Uni<Response> authorization(
            @NotNull(message = "Request body is required") @Valid EmqxAclRequest request) {
        
        log.info("=== EMQX MQTT ACL Request START ===");
        log.info("Request: " + request);
        log.info("Client ID: " + request.getClientId());
        log.info("Username: " + request.getUsername());
        log.info("Topic: " + request.getTopic());
        log.info("Action: " + request.getAction());
        log.info("Access: " + request.getAccess());
        log.info("Peer Host: " + request.getPeerHost());
        
        // TODO: Implement actual authorization logic
        // For now, allow all operations
        EmqxResponse response = EmqxResponse.allow();
        
        log.info("Response: " + response);
        log.info("=== EMQX MQTT ACL Request END ===");
        
        return Uni.createFrom().item(
            Response.ok(response)
                .header("Content-Type", "application/json")
                .build()
        );
    }
}
