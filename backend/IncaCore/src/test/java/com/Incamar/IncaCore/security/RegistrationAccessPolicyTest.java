package com.Incamar.IncaCore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationAccessPolicyTest {

    private RegistrationAccessPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new RegistrationAccessPolicy();
    }

    @Test
    void publicRegistrationDisabledDeniesAnonymousAndRegularUsers() {
        setPublicRegistration(false);

        assertFalse(policy.canRegister(null));
        assertFalse(policy.canRegister(authenticated("ROLE_USER")));
        assertFalse(policy.isPublicDemoRegistration(null));
    }

    @Test
    void administratorCanRegisterWithPublicRegistrationDisabled() {
        setPublicRegistration(false);

        Authentication admin = authenticated("ROLE_ADMIN");
        assertTrue(policy.canRegister(admin));
        assertFalse(policy.isPublicDemoRegistration(admin));
    }

    @Test
    void publicDemoRegistrationAllowsAnonymousAndRegularUsersButNotAdminDemoPath() {
        setPublicRegistration(true);

        assertTrue(policy.canRegister(null));
        assertTrue(policy.isPublicDemoRegistration(null));
        assertTrue(policy.canRegister(authenticated("ROLE_USER")));
        assertTrue(policy.isPublicDemoRegistration(authenticated("ROLE_USER")));
        assertFalse(policy.isPublicDemoRegistration(authenticated("ROLE_ADMIN")));
    }

    @Test
    void unauthenticatedAdminAuthorityCannotBypassDisabledRegistration() {
        setPublicRegistration(false);
        UsernamePasswordAuthenticationToken admin =
                (UsernamePasswordAuthenticationToken) authenticated("ROLE_ADMIN");
        admin.setAuthenticated(false);

        assertFalse(policy.canRegister(admin));
        assertFalse(policy.isPublicDemoRegistration(admin));
    }

    private void setPublicRegistration(boolean enabled) {
        ReflectionTestUtils.setField(policy, "publicDemoRegistrationEnabled", enabled);
    }

    private static Authentication authenticated(String role) {
        return new UsernamePasswordAuthenticationToken(
                "user", "credentials-not-used", List.of(new SimpleGrantedAuthority(role))
        );
    }
}
