package com.example.client.integration;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.client.MainActivity;
import com.example.client.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class MainAppIntegrationTest extends BaseIntegrationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityRule = 
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUpLoggedInUser() {
        // Set up authenticated state
        setupAuthenticatedUser();
    }

    @Test
    public void testNavigationDrawerFunctionality() {
        // Verify drawer is initially closed
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed()));

        // Open navigation drawer
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());

        // Verify drawer is open
        onView(withId(R.id.drawer_layout))
                .check(matches(isOpen()));

        // Verify navigation menu items are visible
        onView(withId(R.id.nav_matches))
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_profile))
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_messages))
                .check(matches(isDisplayed()));
        onView(withId(R.id.nav_settings))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testProfileCreationFlow() throws InterruptedException, IOException {
        // Mock profile creation response
        mockProfileCreation();

        // Navigate to profile
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));

        // Verify profile form is displayed
        onView(withId(R.id.editTextFormation))
                .check(matches(isDisplayed()));
        onView(withId(R.id.editTextSkills))
                .check(matches(isDisplayed()));

        // Fill in profile information
        onView(withId(R.id.editTextFormation))
                .perform(typeText("Computer Science"), closeSoftKeyboard());
        onView(withId(R.id.editTextSkills))
                .perform(typeText("Java,Python,React"), closeSoftKeyboard());

        // Save profile
        onView(withId(R.id.buttonSaveProfile))
                .perform(click());

        // Wait for network call
        waitForNetworkCall();

        // Verify API call was made correctly
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/profile", request.getPath());
        
        String requestBody = request.getBody().readUtf8();
        assert requestBody.contains("Computer Science");
        assert requestBody.contains("Java");

        // Verify success message
        onView(withText(containsString("Profile saved successfully")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testMatchingFlow() throws InterruptedException, IOException {
        // Mock potential matches and match request responses
        mockPotentialMatches();
        mockMatchRequest();

        // Navigate to matches
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_matches));

        // Wait for matches to load
        waitForNetworkCall();

        // Verify matches are displayed
        onView(withId(R.id.recyclerViewMatches))
                .check(matches(isDisplayed()));

        // Verify first match data
        onView(withId(R.id.recyclerViewMatches))
                .check(matches(hasDescendant(withText("alice"))));
        onView(withId(R.id.recyclerViewMatches))
                .check(matches(hasDescendant(withText("Computer Science"))));

        // Test swipe right (like) on first item
        onView(withId(R.id.recyclerViewMatches))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeRight()));

        // Wait for match request
        waitForNetworkCall();

        // Verify match request API call
        mockWebServer.takeRequest(); // Take the initial matches request
        RecordedRequest matchRequest = mockWebServer.takeRequest();
        assertEquals("POST", matchRequest.getMethod());
        assertEquals("/api/matches/request", matchRequest.getPath());
        
        String requestBody = matchRequest.getBody().readUtf8();
        assert requestBody.contains("LIKE");

        // Verify match notification
        onView(withText(containsString("It's a match!")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSwipeToPassFlow() throws InterruptedException, IOException {
        // Mock potential matches
        mockPotentialMatches();
        mockPassResponse();

        // Navigate to matches
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_matches));

        // Wait for matches to load
        waitForNetworkCall();

        // Test swipe left (pass) on first item
        onView(withId(R.id.recyclerViewMatches))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeLeft()));

        // Wait for match request
        waitForNetworkCall();

        // Verify pass request API call
        mockWebServer.takeRequest(); // Take the initial matches request
        RecordedRequest passRequest = mockWebServer.takeRequest();
        assertEquals("POST", passRequest.getMethod());
        assertEquals("/api/matches/request", passRequest.getPath());
        
        String requestBody = passRequest.getBody().readUtf8();
        assert requestBody.contains("PASS");

        // Verify next match is shown
        onView(withId(R.id.recyclerViewMatches))
                .check(matches(hasDescendant(withText("bob"))));
    }

    @Test
    public void testMessagingFlow() throws InterruptedException, IOException {
        // Mock conversations and send message responses
        mockConversations();
        mockSendMessage();

        // Navigate to messages
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_messages));

        // Wait for conversations to load
        waitForNetworkCall();

        // Verify conversations are displayed
        onView(withId(R.id.recyclerViewConversations))
                .check(matches(isDisplayed()));

        // Click on first conversation
        onView(withId(R.id.recyclerViewConversations))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Verify message input is displayed
        onView(withId(R.id.editTextMessage))
                .check(matches(isDisplayed()));
        onView(withId(R.id.buttonSendMessage))
                .check(matches(isDisplayed()));

        // Type and send a message
        onView(withId(R.id.editTextMessage))
                .perform(typeText("Hello!"), closeSoftKeyboard());
        onView(withId(R.id.buttonSendMessage))
                .perform(click());

        // Wait for message to send
        waitForNetworkCall();

        // Verify message appears in conversation
        onView(withText("Hello!"))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testPullToRefreshMatches() throws InterruptedException, IOException {
        // Mock multiple sets of matches for refresh
        mockPotentialMatches();
        mockPotentialMatches(); // Second set for refresh

        // Navigate to matches
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_matches));

        // Wait for initial load
        waitForNetworkCall();

        // Perform pull to refresh
        onView(withId(R.id.swipeRefreshLayout))
                .perform(androidx.test.espresso.action.ViewActions.swipeDown());

        // Wait for refresh
        waitForNetworkCall();

        // Verify refresh indicator disappears
        onView(withId(R.id.recyclerViewMatches))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOfflineMode() {
        // Simulate no network connectivity
        // This would require mocking network state or using no responses

        // Navigate to matches
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_matches));

        // Wait for network timeout
        waitFor(3000);

        // Verify offline message is displayed
        onView(withText(containsString("No internet connection")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSettingsFlow() {
        // Navigate to settings
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_settings));

        // Verify settings options are displayed
        onView(withText("Notifications"))
                .check(matches(isDisplayed()));
        onView(withText("Privacy"))
                .check(matches(isDisplayed()));
        onView(withText("Logout"))
                .check(matches(isDisplayed()));

        // Test logout functionality
        onView(withText("Logout"))
                .perform(click());

        // Verify logout confirmation dialog
        onView(withText("Are you sure you want to logout?"))
                .check(matches(isDisplayed()));
    }

    private void setupAuthenticatedUser() {
        // Set up shared preferences with a mock token
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences("user_prefs", 0)
                .edit()
                .putString("auth_token", "mock.jwt.token")
                .putString("username", "testuser")
                .apply();
    }

    private void mockPassResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"isMatch\":false,\"message\":\"Passed\"}")
                .setHeader("Content-Type", "application/json"));
    }

    private void mockConversations() {
        String conversationsJson = "[" +
                "{\"username\":\"alice\",\"lastMessage\":\"Hey there!\",\"timestamp\":\"2024-01-01T10:00:00Z\"}," +
                "{\"username\":\"bob\",\"lastMessage\":\"How are you?\",\"timestamp\":\"2024-01-01T09:00:00Z\"}" +
                "]";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(conversationsJson)
                .setHeader("Content-Type", "application/json"));
    }

    private void mockSendMessage() {
        String messageJson = "{\"id\":1,\"senderUsername\":\"testuser\",\"content\":\"Hello!\"," +
                "\"timestamp\":\"2024-01-01T10:30:00Z\"}";
        
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(messageJson)
                .setHeader("Content-Type", "application/json"));
    }
}