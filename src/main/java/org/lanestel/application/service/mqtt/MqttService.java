package org.lanestel.application.service.mqtt;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.lanestel.application.dtos.mqtt.request.EmqxAclRequest;
import org.lanestel.application.dtos.mqtt.request.EmqxAuthRequest;
import org.lanestel.application.dtos.mqtt.response.EmqxResponse;
import org.lanestel.domain.usecase.mqtt.IMqttAuthenticationUseCase;
import org.lanestel.domain.usecase.mqtt.IMqttAuthorizationUseCase;

/**
 * Service layer for MQTT operations.
 * Handles authentication and authorization requests from EMQX.
 */
@ApplicationScoped
public class MqttService {
    
    private final IMqttAuthenticationUseCase authenticationUseCase;
    private final IMqttAuthorizationUseCase authorizationUseCase;
    private final Logger log;
    
    /**
     * Constructor with dependency injection
     * @param authenticationUseCase Use case for MQTT authentication
     * @param authorizationUseCase Use case for MQTT authorization
     * @param log Logger for debugging and monitoring
     */
    @Inject
    public MqttService(
        IMqttAuthenticationUseCase authenticationUseCase,
        IMqttAuthorizationUseCase authorizationUseCase,
        Logger log
    ) {
        this.authenticationUseCase = authenticationUseCase;
        this.authorizationUseCase = authorizationUseCase;
        this.log = log;
    }
    
    /**
     * Handles MQTT client authentication
     * @param request The authentication request from EMQX
     * @return Response with authentication result
     */
    public Uni<Response> authenticate(EmqxAuthRequest request) {
        log.info("Processing MQTT authentication request for username: " + request.getUsername());
        
        return authenticationUseCase.authenticate(
                request.getUsername(),
                request.getPassword(),
                request.getClientId()
            )
            .onItem().transform(isAuthenticated -> {
                EmqxResponse emqxResponse = isAuthenticated ? 
                    EmqxResponse.allow() : EmqxResponse.deny();
                
                log.info("Authentication result for username " + request.getUsername() + ": " + 
                        (isAuthenticated ? "ALLOWED" : "DENIED"));
                
                return Response.ok(emqxResponse).build();
            })
            .onFailure().recoverWithItem(throwable -> {
                log.error("Authentication service error", throwable);
                return Response.ok(EmqxResponse.deny()).build();
            });
    }
    
    /**
     * Handles MQTT client authorization
     * @param request The authorization request from EMQX
     * @return Response with authorization result
     */
    public Uni<Response> authorize(EmqxAclRequest request) {
        log.info("Processing MQTT authorization request for username: " + request.getUsername() + 
                ", topic: " + request.getTopic() + ", action: " + request.getAction());
        
        // Use default QoS level 0 for all operations
        int qosLevel = 0;
        
        return authorizationUseCase.authorize(
                request.getUsername(),
                request.getClientId(),
                request.getTopic(),
                request.getAction(),
                request.getQos() != null ? request.getQos() : qosLevel
            )
            .onItem().transform(isAuthorized -> {
                EmqxResponse emqxResponse = isAuthorized ? 
                    EmqxResponse.allow() : EmqxResponse.deny();
                
                log.info("Authorization result for username " + request.getUsername() + 
                        ", topic " + request.getTopic() + ": " + 
                        (isAuthorized ? "ALLOWED" : "DENIED"));
                
                return Response.ok(emqxResponse).build();
            })
            .onFailure().recoverWithItem(throwable -> {
                log.error("Authorization service error", throwable);
                return Response.ok(EmqxResponse.deny()).build();
            });
    }
}
