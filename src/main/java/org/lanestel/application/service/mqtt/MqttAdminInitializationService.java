package org.lanestel.application.service.mqtt;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.lanestel.common.utils.password_util.IPasswordUtil;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttAccountEntity;
import org.lanestel.infrastructures.entity.mqtt_account_entity.MqttPermissionEntity;

/**
 * Service to automatically create admin account when application starts.
 * Admin account will have full permissions on all MQTT topics.
 */
@ApplicationScoped
public class MqttAdminInitializationService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_CLIENT_ID = "ADMIN_CLIENT";
    private static final String ADMIN_PASSWORD = "admin123"; // Should be changed for security

    @Inject
    Logger log;

    @Inject
    IPasswordUtil passwordUtil;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    void onStart(@Observes StartupEvent ev) {
        log.info("Checking for admin account using Mutiny.SessionFactory...");

        this.sessionFactory.withTransaction((session, tx) ->

            session.createQuery("FROM MqttAccountEntity WHERE mqttId = :username", MqttAccountEntity.class)
                .setParameter("username", ADMIN_USERNAME)
                .getSingleResultOrNull()
                .onItem().transformToUni(existingAdmin -> {
                    if (existingAdmin != null) {
                        log.info("Admin account '" + ADMIN_USERNAME + "' already exists. Skipping creation.");
                        return Uni.createFrom().voidItem();
                    } else {
                        log.info("Admin account not found. Creating new admin account...");
                        MqttAccountEntity newAdminAccount = buildAdminAccount();
                        return session.persist(newAdminAccount);
                    }
                })
        )
        .await().indefinitely();

        log.info("Admin initialization check finished.");
    }


    private MqttAccountEntity buildAdminAccount() {
        MqttAccountEntity adminAccount = MqttAccountEntity.builder()
            .mqttId(ADMIN_USERNAME)
            .clientId(ADMIN_CLIENT_ID)
            .mqttPassword(passwordUtil.hash(ADMIN_PASSWORD))
            .build();

        MqttPermissionEntity publishPermission = MqttPermissionEntity.builder()
            .topicPattern("#")
            .action(MqttPermissionEntity.MqttAction.PUBLISH)
            .permission(MqttPermissionEntity.Permission.ALLOW)
            .build();

        MqttPermissionEntity subscribePermission = MqttPermissionEntity.builder()
            .topicPattern("#")
            .action(MqttPermissionEntity.MqttAction.SUBSCRIBE)
            .permission(MqttPermissionEntity.Permission.ALLOW)
            .build();

        adminAccount.addPermission(publishPermission);
        adminAccount.addPermission(subscribePermission);
        return adminAccount;
    }
}