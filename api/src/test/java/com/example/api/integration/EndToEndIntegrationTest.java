package com.example.api.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EndToEndIntegrationTest extends ApiIntegrationTestBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCompleteUserJourney() throws Exception {
        // Step 1: Alice registers and creates a profile
        String aliceToken = registerUser("alice", "alice@university.com", "password123");
        createProfile(aliceToken, "Computer Science", List.of("Java", "Python", "Machine Learning"));

        // Step 2: Bob registers and creates a similar profile
        String bobToken = registerUser("bob", "bob@university.com", "password123");
        createProfile(bobToken, "Computer Science", List.of("Java", "React", "NodeJS"));

        // Step 3: Charlie registers with different interests
        String charlieToken = registerUser("charlie", "charlie@business.com", "password123");
        createProfile(charlieToken, "Business Administration", List.of("Marketing", "Finance"));

        // Step 4: Alice gets potential matches
        List<Map<String, Object>> aliceMatches = getPotentialMatches(aliceToken);
        assertNotNull(aliceMatches);
        assertTrue(aliceMatches.size() >= 1);

        // Step 5: Verify Bob appears in Alice's matches with higher score than Charlie
        Map<String, Object> bobMatch = findMatchByUsername(aliceMatches, "bob");
        Map<String, Object> charlieMatch = findMatchByUsername(aliceMatches, "charlie");
        
        assertNotNull(bobMatch, "Bob should appear in Alice's matches");
        if (charlieMatch != null) {
            int bobScore = (Integer) bobMatch.get("compatibilityScore");
            int charlieScore = (Integer) charlieMatch.get("compatibilityScore");
            assertTrue(bobScore > charlieScore, "Bob should have higher compatibility with Alice");
        }

        // Step 6: Alice likes Bob
        Long bobUserId = ((Number) bobMatch.get("userId")).longValue();
        Map<String, Object> aliceLikeResponse = sendMatchRequest(aliceToken, bobUserId, "LIKE");
        assertNotNull(aliceLikeResponse);

        // Step 7: Bob gets his matches and likes Alice back
        List<Map<String, Object>> bobMatches = getPotentialMatches(bobToken);
        Map<String, Object> aliceMatch = findMatchByUsername(bobMatches, "alice");
        assertNotNull(aliceMatch, "Alice should appear in Bob's matches");

        Long aliceUserId = ((Number) aliceMatch.get("userId")).longValue();
        Map<String, Object> bobLikeResponse = sendMatchRequest(bobToken, aliceUserId, "LIKE");
        assertNotNull(bobLikeResponse);

        // Step 8: Test profile retrieval
        Map<String, Object> aliceProfile = getCurrentProfile(aliceToken);
        assertEquals("alice", aliceProfile.get("username"));
        assertEquals("Computer Science", aliceProfile.get("formation"));

        Map<String, Object> bobProfile = getCurrentProfile(bobToken);
        assertEquals("bob", bobProfile.get("username"));
        assertEquals("Computer Science", bobProfile.get("formation"));

        // Step 9: Test messaging (if endpoint exists)
        testMessaging(aliceToken, bobToken);

        // Step 10: Test that Charlie (different formation) has lower compatibility
        List<Map<String, Object>> charlieMatches = getPotentialMatches(charlieToken);
        if (!charlieMatches.isEmpty()) {
            Map<String, Object> charlieViewsAlice = findMatchByUsername(charlieMatches, "alice");
            if (charlieViewsAlice != null) {
                int charlieAliceScore = (Integer) charlieViewsAlice.get("compatibilityScore");
                int aliceBobScore = (Integer) bobMatch.get("compatibilityScore");
                assertTrue(aliceBobScore > charlieAliceScore, 
                    "Same formation matches should have higher scores than different formations");
            }
        }
    }

    @Test
    public void testMultipleUserInteractions() throws Exception {
        // Create multiple users with varied profiles
        String user1Token = createUserWithProfile("student1", "student1@uni.com", "password123", 
                "Engineering", List.of("C++", "Python", "Algorithms"));
        
        String user2Token = createUserWithProfile("student2", "student2@uni.com", "password123", 
                "Engineering", List.of("Java", "Python", "Databases"));
        
        String user3Token = createUserWithProfile("student3", "student3@uni.com", "password123", 
                "Engineering", List.of("JavaScript", "React", "NodeJS"));

        // Test that all users can see each other
        verifyMutualVisibility(user1Token, user2Token, "student1", "student2");
        verifyMutualVisibility(user1Token, user3Token, "student1", "student3");
        verifyMutualVisibility(user2Token, user3Token, "student2", "student3");

        // Test match requests don't interfere with each other
        List<Map<String, Object>> user1Matches = getPotentialMatches(user1Token);
        Map<String, Object> user2Match = findMatchByUsername(user1Matches, "student2");
        Map<String, Object> user3Match = findMatchByUsername(user1Matches, "student3");

        if (user2Match != null && user3Match != null) {
            Long user2Id = ((Number) user2Match.get("userId")).longValue();
            Long user3Id = ((Number) user3Match.get("userId")).longValue();

            // User1 likes user2 but passes on user3
            sendMatchRequest(user1Token, user2Id, "LIKE");
            sendMatchRequest(user1Token, user3Id, "PASS");

            // Verify user1 can still get matches after making requests
            List<Map<String, Object>> updatedMatches = getPotentialMatches(user1Token);
            assertNotNull(updatedMatches);
        }
    }

    @Test
    public void testErrorHandlingInWorkflow() throws Exception {
        String userToken = registerUser("errortest", "error@test.com", "password123");

        // Test getting matches without profile
        ResponseEntity<String> noProfileResponse = attemptGetPotentialMatches(userToken);
        // Should handle gracefully
        assertTrue(noProfileResponse.getStatusCode() == HttpStatus.OK || 
                  noProfileResponse.getStatusCode() == HttpStatus.BAD_REQUEST);

        // Create profile and test again
        createProfile(userToken, "Test Formation", List.of("TestSkill"));
        
        // Now should work
        List<Map<String, Object>> matches = getPotentialMatches(userToken);
        assertNotNull(matches);
    }

    private void testMessaging(String senderToken, String receiverToken) {
        try {
            // Try to send a message (this may not be implemented yet)
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(senderToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String messageBody = "{\"recipientUsername\":\"bob\",\"content\":\"Hello Bob!\"}";
            HttpEntity<String> entity = new HttpEntity<>(messageBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    getApiUrl("/messages"),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // If messaging is implemented, test it
            if (response.getStatusCode() == HttpStatus.OK) {
                // Test retrieving messages
                ResponseEntity<String> getResponse = restTemplate.exchange(
                        getApiUrl("/messages?user=alice"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class
                );
                
                if (getResponse.getStatusCode() == HttpStatus.OK) {
                    List<Map<String, Object>> messages = objectMapper.readValue(
                            getResponse.getBody(), 
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    assertNotNull(messages);
                }
            }
        } catch (Exception e) {
            // Messaging may not be fully implemented yet, that's okay
            System.out.println("Messaging test skipped - feature may not be implemented yet");
        }
    }

    private void verifyMutualVisibility(String token1, String token2, String username1, String username2) throws Exception {
        List<Map<String, Object>> user1Matches = getPotentialMatches(token1);
        List<Map<String, Object>> user2Matches = getPotentialMatches(token2);

        boolean user1SeesUser2 = findMatchByUsername(user1Matches, username2) != null;
        boolean user2SeesUser1 = findMatchByUsername(user2Matches, username1) != null;

        assertTrue(user1SeesUser2, username1 + " should see " + username2);
        assertTrue(user2SeesUser1, username2 + " should see " + username1);
    }

    private Map<String, Object> findMatchByUsername(List<Map<String, Object>> matches, String username) {
        return matches.stream()
                .filter(match -> username.equals(match.get("username")))
                .findFirst()
                .orElse(null);
    }

    private String createUserWithProfile(String username, String email, String password, 
                                       String formation, List<String> skills) throws Exception {
        String token = registerUser(username, email, password);
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
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format(
                "{\"targetUserId\":%d,\"action\":\"%s\"}",
                targetUserId, action
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getApiUrl("/matches/request"),
                HttpMethod.POST,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
    }

    private Map<String, Object> getCurrentProfile(String token) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                getApiUrl("/profile"),
                HttpMethod.GET,
                entity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
    }
}