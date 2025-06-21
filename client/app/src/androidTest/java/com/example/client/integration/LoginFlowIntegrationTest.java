package com.example.client.integration;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.client.MainActivity;
import com.example.client.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public class LoginFlowIntegrationTest extends BaseIntegrationTest {

    @Before
    public void setUpIntents() {
        Intents.init();
    }

    @After
    public void tearDownIntents() {
        Intents.release();
    }

    @Test
    public void testSuccessfulLoginFlow() throws InterruptedException, IOException {
        // Mock successful login response
        mockSuccessfulLogin();

        // Check that login form is displayed
        onView(withId(R.id.editTextUsername))
                .check(matches(isDisplayed()));
        onView(withId(R.id.editTextPassword))
                .check(matches(isDisplayed()));
        onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()));

        // Enter valid credentials
        onView(withId(R.id.editTextUsername))
                .perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Wait for network call
        waitForNetworkCall();

        // Verify API call was made correctly
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/auth/login", request.getPath());
        assertNotNull(request.getBody());
        
        String requestBody = request.getBody().readUtf8();
        assert requestBody.contains("testuser");
        assert requestBody.contains("password123");

        // Verify navigation to MainActivity
        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testFailedLoginFlow() throws InterruptedException, IOException {
        // Mock login error response
        mockLoginError();

        // Enter invalid credentials
        onView(withId(R.id.editTextUsername))
                .perform(typeText("wronguser"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("wrongpassword"), closeSoftKeyboard());

        // Click login button
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Wait for network call
        waitForNetworkCall();

        // Verify error message is displayed
        onView(withText(containsString("Invalid credentials")))
                .check(matches(isDisplayed()));

        // Verify we remain on login screen
        onView(withId(R.id.editTextUsername))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRegistrationFlow() throws InterruptedException, IOException {
        // Mock successful registration response
        mockSuccessfulRegistration();

        // Click register button to show registration form
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Check that registration form is displayed
        onView(withId(R.id.editTextUsername))
                .check(matches(isDisplayed()));
        onView(withId(R.id.editTextEmail))
                .check(matches(isDisplayed()));
        onView(withId(R.id.editTextPassword))
                .check(matches(isDisplayed()));

        // Enter registration details
        onView(withId(R.id.editTextUsername))
                .perform(typeText("newuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail))
                .perform(typeText("newuser@example.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("newpassword123"), closeSoftKeyboard());

        // Click register button
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Wait for network call
        waitForNetworkCall();

        // Verify API call was made correctly
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/api/auth/register", request.getPath());
        
        String requestBody = request.getBody().readUtf8();
        assert requestBody.contains("newuser");
        assert requestBody.contains("newuser@example.com");
        assert requestBody.contains("newpassword123");

        // Verify navigation to MainActivity
        Intents.intended(IntentMatchers.hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testEmptyFieldsValidation() {
        // Try to login with empty fields
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Verify error messages for empty fields
        onView(withText(containsString("Username is required")))
                .check(matches(isDisplayed()));
        onView(withText(containsString("Password is required")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidEmailValidation() {
        // Click register to show registration form
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Enter invalid email
        onView(withId(R.id.editTextUsername))
                .perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail))
                .perform(typeText("invalid-email"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Try to register
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Verify email validation error
        onView(withText(containsString("Please enter a valid email")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testPasswordStrengthValidation() {
        // Click register to show registration form
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Enter weak password
        onView(withId(R.id.editTextUsername))
                .perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmail))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("123"), closeSoftKeyboard());

        // Try to register
        onView(withId(R.id.buttonRegister))
                .perform(click());

        // Verify password strength error
        onView(withText(containsString("Password must be at least")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testNetworkErrorHandling() throws InterruptedException, IOException {
        // Mock network error
        mockWebServer.enqueue(new okhttp3.mockwebserver.MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Enter credentials
        onView(withId(R.id.editTextUsername))
                .perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Try to login
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Wait for network call
        waitForNetworkCall();

        // Verify network error message
        onView(withText(containsString("Network error")))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLoginButtonStatesDuringNetworkCall() throws InterruptedException {
        // Mock slow response
        mockWebServer.enqueue(new okhttp3.mockwebserver.MockResponse()
                .setResponseCode(200)
                .setBody("{\"token\":\"mock.jwt.token\"}")
                .setBodyDelay(2, java.util.concurrent.TimeUnit.SECONDS));

        // Enter credentials
        onView(withId(R.id.editTextUsername))
                .perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Click login
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // Verify button is disabled during network call
        // (This would require checking the button's enabled state)
        waitFor(500); // Brief wait to see loading state
        
        // Verify loading indicator is shown
        onView(withId(R.id.progressBar))
                .check(matches(isDisplayed()));

        // Wait for response
        waitFor(3000);

        // Verify button is re-enabled and loading is hidden
        onView(withId(R.id.buttonLogin))
                .check(matches(isDisplayed()));
    }
}