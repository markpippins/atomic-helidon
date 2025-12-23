package com.angrysurfer.atomic.helidon.registration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ServiceRegistrationLifecycle {

    @Inject
    private HostServerRegistrationService registrationService;

    @PostConstruct
    public void initialize() {
        registrationService.registerService();
    }

    @PreDestroy
    public void cleanup() {
        registrationService.cleanup();
    }
}