package com.Incamar.IncaCore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("registrationAccessPolicy")
public class RegistrationAccessPolicy {

    @Value("${app.demo.public-registration:false}")
    private boolean publicDemoRegistrationEnabled;

    public boolean canRegister(Authentication authentication) {
        return publicDemoRegistrationEnabled || isAdministrator(authentication);
    }

    public boolean isPublicDemoRegistration(Authentication authentication) {
        return publicDemoRegistrationEnabled && !isAdministrator(authentication);
    }

    private boolean isAdministrator(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
