package com.example.client.integration;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.client.LoginActivity;
import com.example.client.api.ApiClient;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public abstract class BaseIntegrationTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule = 
            new ActivityScenarioRule<>(LoginActivity.class);

    protected MockWebServer mockWebServer;
    protected String baseUrl;

    @Before
    public void setUp() throws IOException {
        // Start MockWebServer
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        baseUrl = mockWebServer.url("/").toString();

        // Clear any stored tokens
        clearSharedPreferences();
        
        // Update ApiClient to use mock server
        setupMockApiClient();
    }

    @After
    public void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    protected void clearSharedPreferences() {
        InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("user_prefs", 0)
                .edit()
                .clear()
                .apply();
    }

    protected void setupMockApiClient() {
        // This would require modifying ApiClient to support test configuration
        // For now, we'll create mock responses for typical scenarios
    }

    protected void mockSuccessfulLogin() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"token\":\"mock.jwt.token\"}")
                .setHeader("Content-Type", "application/json"));
    }

    protected void mockSuccessfulRegistration() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"token\":\"mock.jwt.token\"}")
                .setHeader("Content-Type", "application/json"));
    }

    protected void mockLoginError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\":\"Invalid credentials\"}")
                .setHeader("Content-Type", "application/json"));
    }

    protected void mockPotentialMatches() {
        String matchesJson = "[" +
                "{\"userId\":1,\"username\":\"alice\",\"formation\":\"Computer Science\"," +
                "\"commonSkills\":[\"Java\",\"Python\"],\"compatibilityScore\":85}," +
                "{\"userId\":2,\"username\":\"bob\",\"formation\":\"Engineering\"," +
                "\"commonSkills\":[\"Python\",\"JavaScript\"],\"compatibilityScore\":75}" +
                "]";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(matchesJson)
                .setHeader("Content-Type", "application/json"));
    }

    protected void mockProfileCreation() {
        String profileJson = "{\"username\":\"testuser\",\"formation\":\"Computer Science\"," +
                "\"skills\":[\"Java\",\"Python\"]}";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(profileJson)
                .setHeader("Content-Type", "application/json"));
    }

    protected void mockMatchRequest() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"isMatch\":true,\"message\":\"It's a match!\"}")
                .setHeader("Content-Type", "application/json"));
    }

    protected void waitFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void waitForNetworkCall() {
        waitFor(1000); // Wait for network calls to complete
    }
}