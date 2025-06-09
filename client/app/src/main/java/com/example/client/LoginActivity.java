package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.client.api.ApiClient;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private TextView tvRegisterPrompt;
    private ApiClient apiClient;
    private DebugLogger debugLogger;
    private boolean isRegisterMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize debug logger
        debugLogger = DebugLogger.getInstance(this);
        debugLogger.logAppEvent("LIFECYCLE", "LoginActivity onCreate started");
        
        initViews();
        apiClient = ApiClient.getInstance(this);
        debugLogger.logAppEvent("API_INIT", "ApiClient initialized in LoginActivity");
        
        // Check if already logged in
        if (apiClient.isLoggedIn()) {
            debugLogger.logAppEvent("AUTH_CHECK", "User already logged in, redirecting to main");
            startMainActivity();
            return;
        }
        
        debugLogger.logAppEvent("AUTH_CHECK", "User not logged in, showing login screen");
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        debugLogger.logScreenView("LOGIN");
    }
    
    private void initViews() {
        debugLogger.logAction("UI_INIT", "LOGIN", "Initializing UI components");
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvRegisterPrompt = findViewById(R.id.tvRegisterPrompt);
    }
    
    private void setupClickListeners() {
        debugLogger.logAction("UI_INIT", "LOGIN", "Setting up click listeners");
        btnLogin.setOnClickListener(v -> {
            debugLogger.logButtonClick("LOGIN", "LOGIN_BUTTON");
            handleLogin();
        });
        btnRegister.setOnClickListener(v -> {
            debugLogger.logButtonClick("LOGIN", "REGISTER_BUTTON");
            handleRegister();
        });
        tvRegisterPrompt.setOnClickListener(v -> {
            debugLogger.logButtonClick("LOGIN", "TOGGLE_MODE_LINK");
            toggleMode();
        });
    }
    
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        debugLogger.logUserAction("LOGIN", "MODE_TOGGLE", isRegisterMode ? "Switched to register mode" : "Switched to login mode");
        
        if (isRegisterMode) {
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            tvRegisterPrompt.setText("Already have an account? Login");
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            tvRegisterPrompt.setText("Don't have an account? Register");
        }
    }
    
    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        debugLogger.logUserAction("LOGIN", "LOGIN_ATTEMPT", "Username: " + username);
        
        if (username.isEmpty() || password.isEmpty()) {
            debugLogger.logError("LOGIN", "LOGIN", "Empty fields validation", "Username or password is empty");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoading(true);
        debugLogger.logApiCall("LOGIN", "/api/auth/login", "POST");
        
        ApiModels.LoginRequest request = new ApiModels.LoginRequest(username, password);
        Call<ApiModels.AuthResponse> call = apiClient.getApiService().login(request);
        
        call.enqueue(new Callback<ApiModels.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiModels.AuthResponse> call, Response<ApiModels.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("LOGIN", "/api/auth/login", "POST");
                    debugLogger.logAppEvent("AUTH_SUCCESS", "Login successful for user: " + username);
                    
                    String token = response.body().getToken();
                    apiClient.saveToken(token);
                    apiClient.saveUsername(username);
                    debugLogger.setUserId(username);
                    
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    debugLogger.logApiError("LOGIN", "/api/auth/login", "POST", "Response code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Login failed. Check credentials.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.AuthResponse> call, Throwable t) {
                setLoading(false);
                debugLogger.logApiError("LOGIN", "/api/auth/login", "POST", "Network failure: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode due to network error");
                    // Enable mock mode and retry
                    apiClient.enableMockMode(true);
                    Toast.makeText(LoginActivity.this, "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    
                    // Retry the login with mock service
                    retryLoginWithMock();
                } else {
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private boolean isNetworkError(Throwable t) {
        return t instanceof java.net.ConnectException || 
               t instanceof java.net.SocketTimeoutException ||
               t instanceof java.io.IOException;
    }
    
    private void retryLoginWithMock() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        debugLogger.logApiCall("LOGIN", "/api/auth/login (mock)", "POST");
        setLoading(true);
        
        ApiModels.LoginRequest request = new ApiModels.LoginRequest(username, password);
        Call<ApiModels.AuthResponse> call = apiClient.getApiService().login(request);
        
        call.enqueue(new Callback<ApiModels.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiModels.AuthResponse> call, Response<ApiModels.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("LOGIN", "/api/auth/login (mock)", "POST");
                    debugLogger.logAppEvent("AUTH_SUCCESS", "Mock login successful for user: " + username);
                    
                    String token = response.body().getToken();
                    apiClient.saveToken(token);
                    apiClient.saveUsername(username);
                    debugLogger.setUserId(username);
                    
                    Toast.makeText(LoginActivity.this, "Login successful (offline mode)!", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    debugLogger.logApiError("LOGIN", "/api/auth/login (mock)", "POST", "Mock response failed: " + response.code());
                    Toast.makeText(LoginActivity.this, "Login failed even in offline mode.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.AuthResponse> call, Throwable t) {
                setLoading(false);
                debugLogger.logApiError("LOGIN", "/api/auth/login (mock)", "POST", "Mock failure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, "Offline mode failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        debugLogger.logUserAction("LOGIN", "REGISTER_ATTEMPT", "Username: " + username);
        
        if (username.isEmpty() || password.isEmpty()) {
            debugLogger.logError("LOGIN", "REGISTER", "Empty fields validation", "Username or password is empty");
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For registration, we'll use username as email (simplified)
        String email = username + "@example.com";
        
        setLoading(true);
        debugLogger.logApiCall("LOGIN", "/api/auth/register", "POST");
        
        ApiModels.RegisterRequest request = new ApiModels.RegisterRequest(username, email, password);
        Call<ApiModels.AuthResponse> call = apiClient.getApiService().register(request);
        
        call.enqueue(new Callback<ApiModels.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiModels.AuthResponse> call, Response<ApiModels.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("LOGIN", "/api/auth/register", "POST");
                    debugLogger.logAppEvent("AUTH_SUCCESS", "Registration successful for user: " + username);
                    
                    String token = response.body().getToken();
                    apiClient.saveToken(token);
                    apiClient.saveUsername(username);
                    debugLogger.setUserId(username);
                    
                    Toast.makeText(LoginActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    debugLogger.logApiError("LOGIN", "/api/auth/register", "POST", "Response code: " + response.code());
                    Toast.makeText(LoginActivity.this, "Registration failed. Username might be taken.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.AuthResponse> call, Throwable t) {
                setLoading(false);
                debugLogger.logApiError("LOGIN", "/api/auth/register", "POST", "Network failure: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for registration due to network error");
                    // Enable mock mode and retry
                    apiClient.enableMockMode(true);
                    Toast.makeText(LoginActivity.this, "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    
                    // Retry the registration with mock service
                    retryRegisterWithMock();
                } else {
                    Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void retryRegisterWithMock() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = username + "@example.com";
        
        setLoading(true);
        
        ApiModels.RegisterRequest request = new ApiModels.RegisterRequest(username, email, password);
        Call<ApiModels.AuthResponse> call = apiClient.getApiService().register(request);
        
        call.enqueue(new Callback<ApiModels.AuthResponse>() {
            @Override
            public void onResponse(Call<ApiModels.AuthResponse> call, Response<ApiModels.AuthResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    apiClient.saveToken(token);
                    apiClient.saveUsername(username);
                    Toast.makeText(LoginActivity.this, "Registration successful (offline mode)!", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Registration failed even in offline mode.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Offline mode failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnRegister.setEnabled(!loading);
        etUsername.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
} 