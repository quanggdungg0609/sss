package org.lanestel.application.service.device;

import java.util.Map;
import java.util.HashMap;

import org.lanestel.application.dtos.device.CreateDeviceRequest;
import org.lanestel.domain.usecase.device.ICreateDeviceUseCase;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
public class DeviceService {
    private final ICreateDeviceUseCase createDeviceUseCase;

    @Inject
    public DeviceService(ICreateDeviceUseCase createDeviceUseCase) {
        this.createDeviceUseCase = createDeviceUseCase;
    }

    /**
     * Creates a new device with MQTT account.
     * 
     * This method delegates to the CreateDeviceUseCase to handle the business logic
     * and returns an appropriate HTTP response based on the operation result.
     *
     * @param request the device creation request containing device name and MQTT username
     * @return Uni<Response> a reactive HTTP response containing the created device data or error
     */
    public Uni<Response> createDevice(CreateDeviceRequest request){
        return createDeviceUseCase.execute(request.getDeviceName(), request.getMqttUsername())
            .onItem().transform(devicePOJO -> {
                // Success: return 201 Created with device data
                return Response.status(Status.CREATED)
                    .entity(devicePOJO)
                    .build();
            })
            .onFailure().recoverWithItem(throwable -> {
                // Handle different types of exceptions
                if (throwable.getMessage().contains("already exists")) {
                    // Conflict: MQTT username already exists
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Conflict");
                    errorResponse.put("message", throwable.getMessage());
                    
                    return Response.status(Status.CONFLICT)
                        .entity(errorResponse)
                        .build();
                } else {
                    // Internal server error for other exceptions
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Internal Server Error");
                    errorResponse.put("message", "Failed to create device");
                    
                    return Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(errorResponse)
                        .build();
                }
            });
    }
}
