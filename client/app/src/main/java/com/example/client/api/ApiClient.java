package com.example.client.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // Default URLs for different environments
    private static final String DEFAULT_EMULATOR_URL = "http://10.0.2.2:8080/";
    private static final String DEFAULT_DEVICE_URL = "http://172.20.10.9:8080/";
    
    private static final String PREFS_NAME = "BinomePrefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String USERNAME_KEY = "username";
    private static final String MOCK_MODE_KEY = "mock_mode";
    private static final String BASE_URL_KEY = "base_url";
    
    private static ApiClient instance;
    private BinomeApiService apiService;
    private MockApiService mockApiService;
    private SharedPreferences sharedPreferences;
    private boolean isMockMode = false;
    private String baseUrl;
    
    private ApiClient(Context context) {
        // Initialize SharedPreferences for token storage
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Get base URL from preferences or use default
        baseUrl = sharedPreferences.getString(BASE_URL_KEY, DEFAULT_EMULATOR_URL);
        
        // Check if we should use mock mode
        isMockMode = sharedPreferences.getBoolean(MOCK_MODE_KEY, false);
        
        // Create HTTP client with logging and authentication
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // Authentication interceptor
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                
                // Skip auth for login/register endpoints
                String path = original.url().encodedPath();
                if (path.contains("/auth/login") || path.contains("/auth/register")) {
                    return chain.proceed(original);
                }
                
                // Add authorization header for other endpoints
                String token = getToken();
                if (token != null) {
                    Request authenticated = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(authenticated);
                }
                
                return chain.proceed(original);
            }
        };
        
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
        
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(BinomeApiService.class);
        mockApiService = new MockApiService(context);
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }
    
    public BinomeApiService getApiService() {
        return isMockMode ? mockApiService : apiService;
    }
    
    public void enableMockMode(boolean enable) {
        isMockMode = enable;
        sharedPreferences.edit().putBoolean(MOCK_MODE_KEY, enable).apply();
    }
    
    public boolean isMockMode() {
        return isMockMode;
    }
    
    // Token management methods
    public void saveToken(String token) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
    }
    
    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }
    
    public void saveUsername(String username) {
        sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
    }
    
    public String getUsername() {
        return sharedPreferences.getString(USERNAME_KEY, null);
    }
    
    public boolean isLoggedIn() {
        return getToken() != null;
    }
    
    public void logout() {
        sharedPreferences.edit().clear().apply();
    }
    
    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }
    
    // Method to update base URL
    public void setBaseUrl(String newBaseUrl) {
        this.baseUrl = newBaseUrl;
        sharedPreferences.edit().putString(BASE_URL_KEY, newBaseUrl).apply();
        // Note: Changing base URL requires app restart to take effect
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    // Helper method to use appropriate URL based on environment
    public void useEmulatorUrl() {
        setBaseUrl(DEFAULT_EMULATOR_URL);
    }
    
    public void useDeviceUrl() {
        setBaseUrl(DEFAULT_DEVICE_URL);
    }
} 