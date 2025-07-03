package org.lanestel.domain.usecase.mqtt;

import io.smallrye.mutiny.Uni;

/**
 * Interface for MQTT authentication use case.
 * Handles authentication of MQTT clients against stored credentials.
 */
public interface IMqttAuthenticationUseCase {
    /**
     * Authenticates an MQTT client using username and password
     * @param username The MQTT username (mqttId)
     * @param password The plain text password to verify
     * @param clientId The client ID for additional validation
     * @return Uni<Boolean> true if authentication successful, false otherwise
     */
    Uni<Boolean> authenticate(String username, String password, String clientId);
}