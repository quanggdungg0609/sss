package org.lanestel.application.exception.device;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;
import org.lanestel.application.exception.mqtt.MqttAccountAlreadyExistsException;
import org.lanestel.infrastructures.exception.device.CreateDeviceException;
import java.util.Map;

/**
 * Exception handler specifically for device-related operations.
 * Handles all exceptions thrown by device controllers and services.
 */
@Provider
public class DeviceExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    private UriInfo uriInfo;

    @Inject
    private Logger log;

    /**
     * Maps device-related exceptions to appropriate HTTP responses with dynamic path.
     *
     * @param exception the exception to be handled
     * @return Response with appropriate status code and error message
     */
    @Override
    public Response toResponse(Exception exception) {
        // Get the actual request path dynamically
        String requestPath = uriInfo.getPath();
        log.error(exception);
        // Handle device-specific exceptions
        if (exception instanceof MqttAccountAlreadyExistsException) {
            return Response.status(Response.Status.CONFLICT)
                .entity(Map.of(
                    "error", "Conflict",
                    "message", exception.getMessage(),
                    "path",  requestPath
                ))
                .build();
        }
        
        if (exception instanceof CreateDeviceException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                    "error", "Internal Server Error",
                    "message", "Failed to create device due to system error: " + exception.getMessage(),
                    "path",requestPath
                ))
                .build();
        }
        
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "Invalid Input",
                    "message", exception.getMessage(),
                    "path", requestPath
                ))
                .build();
        }
        
        // Generic device error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(Map.of(
                "error", "Internal Server Error",
                "message", "An unexpected error occurred in device module",
                "path", requestPath
            ))
            .build();
    }
}