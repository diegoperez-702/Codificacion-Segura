package com.example.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityServiceTest {

    @Autowired
    private SecurityService securityService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        UserDetails userDetails = securityService.loadUserByUsername("user");
        assertNotNull(userDetails);
        assertEquals("user", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionForUnknownUser() {
        assertThrows(UsernameNotFoundException.class, () -> securityService.loadUserByUsername("unknown"));
    }
}
