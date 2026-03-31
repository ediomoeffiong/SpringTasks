package com.fifthlab.springtasks.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Use a dummy secret (must be at least 256 bits for HS256)
        String secret = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
        jwtUtil = new JwtUtil(secret, 3600000); // 1 hour expiration
        userDetails = new User("testuser", "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);

        String username = jwtUtil.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }
}
