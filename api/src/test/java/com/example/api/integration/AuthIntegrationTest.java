package com.example.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AuthIntegrationTest extends ApiIntegrationTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCompleteAuthenticationFlow() throws Exception {
        // Test user registration
        String registrationResponse = registerUser("testuser", "test@example.com", "password123");
        assertNotNull(registrationResponse);
        assertTrue(registrationResponse.length() > 10); // Basic JWT length check

        // Test duplicate registration should fail
        ResponseEntity<String> duplicateResponse = attemptRegisterUser("testuser", "test2@example.com", "password123");
        assertEquals(HttpStatus.CONFLICT, duplicateResponse.getStatusCode());

        // Test login with correct credentials
        String loginResponse = loginUser("testuser", "password123");
        assertNotNull(loginResponse);
        assertEquals(registrationResponse, loginResponse); // Should get same or new valid token

        // Test login with incorrect password
        ResponseEntity<String> wrongPasswordResponse = attemptLoginUser("testuser", "wrongpassword");
        assertEquals(HttpStatus.UNAUTHORIZED, wrongPasswordResponse.getStatusCode());

        // Test login with non-existent user
        ResponseEntity<String> nonExistentUserResponse = attemptLoginUser("nonexistent", "password123");
        assertEquals(HttpStatus.UNAUTHORIZED, nonExistentUserResponse.getStatusCode());
    }

    @Test
    public void testJwtTokenValidation() throws Exception {
        // Register and login to get a valid token
        String token = registerUser("tokenuser", "token@example.com", "password123");

        // Test accessing protected endpoint with valid token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> protectedResponse = restTemplate.exchange(
                getApiUrl("/profile"),
                HttpMethod.GET,
                entity,
                String.class
        );

        // Should be able to access protected endpoint (even if it returns 404, auth should pass)
        assertNotEquals(HttpStatus.UNAUTHORIZED, protectedResponse.getStatusCode());

        // Test accessing protected endpoint without token
        ResponseEntity<String> noTokenResponse = restTemplate.exchange(
                getApiUrl("/profile"),
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, noTokenResponse.getStatusCode());

        // Test accessing protected endpoint with invalid token
        HttpHeaders invalidHeaders = new HttpHeaders();
        invalidHeaders.setBearerAuth("invalid.token.here");
        HttpEntity<String> invalidEntity = new HttpEntity<>(invalidHeaders);
        
        ResponseEntity<String> invalidTokenResponse = restTemplate.exchange(
                getApiUrl("/profile"),
                HttpMethod.GET,
                invalidEntity,
                String.class
        );
        assertEquals(HttpStatus.UNAUTHORIZED, invalidTokenResponse.getStatusCode());
    }

    @Test
    public void testEmailValidation() throws Exception {
        // Test registration with invalid email formats
        ResponseEntity<String> invalidEmailResponse1 = attemptRegisterUser("user1", "invalid-email", "password123");
        assertEquals(HttpStatus.BAD_REQUEST, invalidEmailResponse1.getStatusCode());

        ResponseEntity<String> invalidEmailResponse2 = attemptRegisterUser("user2", "invalid@", "password123");
        assertEquals(HttpStatus.BAD_REQUEST, invalidEmailResponse2.getStatusCode());

        ResponseEntity<String> invalidEmailResponse3 = attemptRegisterUser("user3", "@invalid.com", "password123");
        assertEquals(HttpStatus.BAD_REQUEST, invalidEmailResponse3.getStatusCode());
    }

    @Test
    public void testPasswordValidation() throws Exception {
        // Test registration with weak passwords
        ResponseEntity<String> shortPasswordResponse = attemptRegisterUser("user1", "test@example.com", "123");
        assertEquals(HttpStatus.BAD_REQUEST, shortPasswordResponse.getStatusCode());

        ResponseEntity<String> emptyPasswordResponse = attemptRegisterUser("user2", "test2@example.com", "");
        assertEquals(HttpStatus.BAD_REQUEST, emptyPasswordResponse.getStatusCode());
    }

    private String registerUser(String username, String email, String password) throws Exception {
        ResponseEntity<String> response = attemptRegisterUser(username, email, password);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        return (String) responseBody.get("token");
    }

    private ResponseEntity<String> attemptRegisterUser(String username, String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                username, email, password
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        return restTemplate.exchange(
                getApiUrl("/auth/register"),
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private String loginUser(String username, String password) throws Exception {
        ResponseEntity<String> response = attemptLoginUser(username, password);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        return (String) responseBody.get("token");
    }

    private ResponseEntity<String> attemptLoginUser(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\"}",
                username, password
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        
        return restTemplate.exchange(
                getApiUrl("/auth/login"),
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}