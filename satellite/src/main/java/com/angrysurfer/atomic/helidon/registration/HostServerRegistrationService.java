package com.angrysurfer.atomic.helidon.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@ApplicationScoped
public class HostServerRegistrationService {

    private static final Logger logger = Logger.getLogger(HostServerRegistrationService.class.getName());

    @Inject
    @ConfigProperty(name = "host.server.url", defaultValue = "http://localhost:8085")
    String hostServerUrl;

    @Inject
    @ConfigProperty(name = "server.port", defaultValue = "8080")
    int port;

    @Inject
    @ConfigProperty(name = "service.name", defaultValue = "satellite")
    String serviceName;

    @Inject
    @ConfigProperty(name = "service.host", defaultValue = "localhost")
    String serviceHost;

    @Inject
    @ConfigProperty(name = "registration.enabled", defaultValue = "true")
    boolean registrationEnabled;

    @Inject
    @ConfigProperty(name = "heartbeat.interval.seconds", defaultValue = "30")
    int heartbeatInterval;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private ScheduledExecutorService scheduler;

    public HostServerRegistrationService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Register the service with the host-server
     */
    public void registerService() {
        if (!registrationEnabled) {
            logger.info("Host server registration is disabled");
            return;
        }

        logger.info("Starting registration with host server: " + hostServerUrl);

        // Create registration payload
        Map<String, Object> registration = new HashMap<>();
        registration.put("serviceName", serviceName);
        registration.put("operations", List.of("simple-greet", "greet", "health"));
        registration.put("endpoint", String.format("http://%s:%d", serviceHost, port));
        registration.put("healthCheck", String.format("http://%s:%d/health", serviceHost, port));
        registration.put("framework", "Helidon MP");
        registration.put("version", "4.3.2");
        registration.put("port", port);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "helidon-service");
        metadata.put("language", "Java");
        metadata.put("runtime", "Helidon");
        metadata.put("native-capable", true);
        registration.put("metadata", metadata);

        // Send registration request
        try {
            String jsonPayload = objectMapper.writeValueAsString(registration);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostServerUrl + "/api/registry/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                logger.info("Successfully registered with host server: " + serviceName);
                logger.info("Service endpoint: " + String.format("http://%s:%d", serviceHost, port));
                
                // Start heartbeat scheduler
                startHeartbeat();
            } else {
                logger.warning("Failed to register with host server. Status: " + response.statusCode() + 
                              ", Response: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error registering with host server: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.severe("Error registering with host server: " + e.getMessage());
        }
    }

    /**
     * Send heartbeat to maintain registration
     */
    private void sendHeartbeat() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hostServerUrl + "/api/registry/heartbeat/" + serviceName))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                logger.info("Heartbeat sent successfully for: " + serviceName);
            } else {
                logger.warning("Failed to send heartbeat. Status: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.warning("Error sending heartbeat: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warning("Error sending heartbeat: " + e.getMessage());
        }
    }

    /**
     * Start the heartbeat scheduler
     */
    private void startHeartbeat() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            this::sendHeartbeat,
            heartbeatInterval,
            heartbeatInterval,
            TimeUnit.SECONDS
        );
        logger.info("Heartbeat scheduler started with interval: " + heartbeatInterval + " seconds");
    }

    /**
     * Cleanup resources on shutdown
     */
    public void cleanup() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}