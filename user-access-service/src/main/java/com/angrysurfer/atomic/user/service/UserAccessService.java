package com.angrysurfer.atomic.user.service;

import com.angrysurfer.atomic.user.UserDTO;
import com.angrysurfer.atomic.user.UserRegistrationDTO;
import com.angrysurfer.atomic.user.model.UserRegistration;
import com.angrysurfer.atomic.user.repository.UserRegistrationRepository;
import com.angrysurfer.atomic.broker.spi.BrokerOperation;
import com.angrysurfer.atomic.broker.spi.BrokerParam;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class UserAccessService {

    private static final Logger log = Logger.getLogger(UserAccessService.class.getName());

    @Inject
    private UserRegistrationRepository userRepository;

    public UserAccessService() {
        log.info("UserAccessService initialized");
    }

    @BrokerOperation("validateUser")
    public UserRegistrationDTO validateUser(@BrokerParam("alias") String alias, @BrokerParam("identifier") String password) {

        log.info("Validating user " + alias);
        UserRegistration userReg = userRepository.findByAlias(alias).orElse(null);

        if (userReg == null || !userReg.getIdentifier().equals(password)) {
            return null;
        }

        return userReg.toDTO();
    }
}