package org.lanestel.infrastructures.components.notification;

import java.util.List;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;
import org.lanestel.infrastructures.components.threshold_check.ThresholdCheckComponent;


@ApplicationScoped
public class NotificationComponent {
    @Inject
    ReactiveMailer mailer;

    @Inject
    Logger log;

    public Uni<Void> sendViolationAlert(Long deviceId, List<ThresholdCheckComponent.Violation> violations) {
         if (violations == null || violations.isEmpty()) {
            return Uni.createFrom().voidItem();
        }

        // Log the violation details first
        log.errorf("--- THRESHOLD VIOLATION ALERT for deviceId %d ---", deviceId);
        violations.forEach(v -> log.errorf(" > %s", v.message()));
        log.error("----------------------------------------------------");

        // Build email content
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Threshold violation alert for device ID: ").append(deviceId).append("\n\n");
        emailBody.append("Violation details:\n");
        for (ThresholdCheckComponent.Violation v : violations) {
            emailBody.append("- ").append(v.message()).append("\n");
        }

        // Create Mail object
        // Mail mail = Mail.withText(
        //     "truongquangdung0609@gmail.com", 
        //     "[ALERT] Threshold violation for device " + deviceId,
        //     emailBody.toString()
        // );
        Mail mail = new Mail()
            .addTo("truongquangdung0609@gmail.com")
            .addTo("pjoffre@lanestel.fr") 
            .setSubject("[ALERT] Threshold violation for device " + deviceId)
            .setText(emailBody.toString());

        log.info("Attempting to send violation email for deviceId: " + deviceId);
        // Send email asynchronously and return the Uni
        return mailer.send(mail);
    }
}
