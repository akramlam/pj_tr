package com.example.api.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MatchingIntegrationTest extends ApiIntegrationTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String userToken1;
    private String userToken2;
    private String userToken3;

    @BeforeEach
    public void setupUsers() throws Exception {
        // Create test users with different profiles
        userToken1 = createUserWithProfile("user1", "user1@example.com", "password123", 
                "Computer Science", List.of("Java", "Python", "React"));
        
        userToken2 = createUserWithProfile("user2", "user2@example.com", "password123", 
                "Computer Science", List.of("Java", "JavaScript", "Angular"));
        
        userToken3 = createUserWithProfile("user3", "user3@example.com", "password123", 
                "Business Administration", List.of("Marketing", "Sales", "Management"));
    }

    @Test
    public void testCompleteMatchingWorkflow() throws Exception {
        // Test getting potential matches for user1
        List<Map<String, Object>> potentialMatches = getPotentialMatches(userToken1);
        assertNotNull(potentialMatches);
        assertTrue(potentialMatches.size() >= 1); // Should find at least user2 as a match

        // Verify match data structure
        Map<String, Object> firstMatch = potentialMatches.get(0);
        assertNotNull(firstMatch.get("userId"));
        assertNotNull(firstMatch.get("username"));
        assertNotNull(firstMatch.get("formation"));
        assertNotNull(firstMatch.get("commonSkills"));
        assertNotNull(firstMatch.get("compatibilityScore"));

        // Test that computer science users have higher compatibility
        boolean foundComputerScienceMatch = potentialMatches.stream()
                .anyMatch(match -> "Computer Science".equals(match.get("formation")));
        assertTrue(foundComputerScienceMatch);

        // Test sending a LIKE match request
        Long targetUserId = ((Number) firstMatch.get("userId")).longValue();
        Map<String, Object> likeResponse = sendMatchRequest(userToken1, targetUserId, "LIKE");
        assertNotNull(likeResponse);
        assertNotNull(likeResponse.get("message"));
        assertNotNull(likeResponse.get("isMatch"));

        // Test sending a PASS match request
        if (potentialMatches.size() > 1) {
            Long targetUserId2 = ((Number) potentialMatches.get(1).get("userId")).longValue();
            Map<String, Object> passResponse = sendMatchRequest(userToken1, targetUserId2, "PASS");
            assertNotNull(passResponse);
            assertEquals("Passed", passResponse.get("message"));
            assertEquals(false, passResponse.get("isMatch"));
        }
    }

    @Test
    public void testMatchingAlgorithmAccuracy() throws Exception {
        // Get potential matches for user1 (Computer Science)
        List<Map<String, Object>> user1Matches = getPotentialMatches(userToken1);
        
        // Verify that user2 (Computer Science) has higher compatibility than user3 (Business)
        Map<String, Object> user2Match = user1Matches.stream()
                .filter(match -> "user2".equals(match.get("username")))
                .findFirst()
                .orElse(null);
        
        Map<String, Object> user3Match = user1Matches.stream()
                .filter(match -> "user3".equals(match.get("username")))
                .findFirst()
                .orElse(null);

        if (user2Match != null && user3Match != null) {
            int user2Score = (Integer) user2Match.get("compatibilityScore");
            int user3Score = (Integer) user3Match.get("compatibilityScore");
            assertTrue(user2Score > user3Score, "Same formation should have higher compatibility");
        }
    }

    @Test
    public void testMatchingWithoutProfile() throws Exception {
        // Create a user without a profile
        String noProfileToken = registerUser("noprofile", "noprofile@example.com", "password123");
        
        // Try to get potential matches without having a profile
        ResponseEntity<String> response = attemptGetPotentialMatches(noProfileToken);
        
        // Should handle gracefully (either empty list or appropriate error)
        assertTrue(response.getStatusCode() == HttpStatus.OK || 
                  response.getStatusCode() == HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testMatchRequestValidation() throws Exception {
        // Test match request with invalid user ID
        ResponseEntity<String> invalidUserResponse = attemptSendMatchRequest(userToken1, 99999L, "LIKE");
        assertTrue(invalidUserResponse.getStatusCode() == HttpStatus.NOT_FOUND ||
                  invalidUserResponse.getStatusCode() == HttpStatus.BAD_REQUEST);

        // Test match request with invalid action
        ResponseEntity<String> invalidActionResponse = attemptSendMatchRequest(userToken1, 1L, "INVALID_ACTION");
        assertEquals(HttpStatus.BAD_REQUEST, invalidActionResponse.getStatusCode());
    }

    @Test
    public void testCrossUserMatching() throws Exception {
        // Test that user1 and user2 can see each other in potential matches
        List<Map<String, Object>> user1Matches = getPotentialMatches(userToken1);
        List<Map<String, Object>> user2Matches = getPotentialMatches(userToken2);

        boolean user1SeesUser2 = user1Matches.stream()
                .anyMatch(match -> "user2".equals(match.get("username")));
        boolean user2SeesUser1 = user2Matches.stream()
                .anyMatch(match -> "user1".equals(match.get("username")));

        assertTrue(user1SeesUser2, "User1 should see User2 as a potential match");
        assertTrue(user2SeesUser1, "User2 should see User1 as a potential match");
    }

    private String createUserWithProfile(String username, String email, String password, 
                                       String formation, List<String> skills) throws Exception {
        // Register user
        String token = registerUser(username, email, password);
        
        // Create profile
        createProfile(token, formation, skills);
        
        return token;
    }

    private String registerUser(String username, String email, String password) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
                "{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                username, email, password
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getApiUrl("/auth/register"),
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        return (String) responseBody.get("token");
    }

    private void createProfile(String token, String formation, List<String> skills) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String skillsJson = objectMapper.writeValueAsString(skills);
        String requestBody = String.format(
                "{\"formation\":\"%s\",\"skills\":%s}",
                formation, skillsJson
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getApiUrl("/profile"),
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private List<Map<String, Object>> getPotentialMatches(String token) throws Exception {
        ResponseEntity<String> response = attemptGetPotentialMatches(token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        return objectMapper.readValue(response.getBody(), new TypeReference<List<Map<String, Object>>>() {});
    }

    private ResponseEntity<String> attemptGetPotentialMatches(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                getApiUrl("/matches/potential"),
                HttpMethod.GET,
                entity,
                String.class
        );
    }

    private Map<String, Object> sendMatchRequest(String token, Long targetUserId, String action) throws Exception {
        ResponseEntity<String> response = attemptSendMatchRequest(token, targetUserId, action);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
    }

    private ResponseEntity<String> attemptSendMatchRequest(String token, Long targetUserId, String action) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
                "{\"targetUserId\":%d,\"action\":\"%s\"}",
                targetUserId, action
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(
                getApiUrl("/matches/request"),
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}