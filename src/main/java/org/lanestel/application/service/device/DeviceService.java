package org.lanestel.application.service.device;


import org.lanestel.application.dtos.device.request.CreateDeviceRequest;
import org.lanestel.application.dtos.device.response.CreateDeviceResponse;
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
                    .entity(
                        CreateDeviceResponse.builder()
                            .deviceName(devicePOJO.getDeviceName())
                            .mqttAccount(devicePOJO.getMqttId())
                            .mqttPassword(devicePOJO.getMqttPassword())
                            .clientId(devicePOJO.getClientId())
                            .build()
                    )
                    .build();
            });
    }
}
