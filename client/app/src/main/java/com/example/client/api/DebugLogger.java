package com.example.client.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DebugLogger {
    private static final String TAG = "DebugLogger";
    private static final String DEBUG_URL = "http://10.0.2.2:8080/api/debug/log";
    private static final String PREF_NAME = "debug_settings";
    private static final String PREF_USER_ID = "user_id";
    
    private static DebugLogger instance;
    private final Context context;
    private final ExecutorService executor;
    private final String deviceInfo;
    private final String appVersion;
    private boolean isEnabled = true;
    
    private DebugLogger(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.deviceInfo = buildDeviceInfo();
        this.appVersion = getAppVersion();
    }
    
    public static synchronized DebugLogger getInstance(Context context) {
        if (instance == null) {
            instance = new DebugLogger(context);
        }
        return instance;
    }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        Log.d(TAG, "Debug logging " + (enabled ? "enabled" : "disabled"));
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setUserId(String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_USER_ID, userId).apply();
    }
    
    private String getUserId() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_USER_ID, "unknown_user");
    }
    
    public void logAction(String action, String screen, String details) {
        logAction(action, screen, details, null);
    }
    
    public void logError(String action, String screen, String details, String errorMessage) {
        logAction(action, screen, details, errorMessage);
    }
    
    public void logAction(String action, String screen, String details, String errorMessage) {
        if (!isEnabled) {
            return;
        }
        
        // Always log to Android system log for immediate debugging
        String logMessage = String.format("[%s] %s in %s: %s", action, getUserId(), screen, details);
        if (errorMessage != null) {
            Log.e(TAG, logMessage + " ERROR: " + errorMessage);
        } else {
            Log.i(TAG, logMessage);
        }
        
        // Send to backend asynchronously
        executor.execute(() -> sendLogToBackend(action, screen, details, errorMessage));
    }
    
    private void sendLogToBackend(String action, String screen, String details, String errorMessage) {
        try {
            JSONObject logData = new JSONObject();
            logData.put("userId", getUserId());
            logData.put("action", action);
            logData.put("screen", screen);
            logData.put("details", details);
            logData.put("deviceInfo", deviceInfo);
            logData.put("appVersion", appVersion);
            
            if (errorMessage != null) {
                logData.put("errorMessage", errorMessage);
            }
            
            URL url = new URL(DEBUG_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Write JSON data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = logData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Debug log sent successfully");
            } else {
                Log.w(TAG, "Failed to send debug log. Response code: " + responseCode);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send debug log to backend", e);
        }
    }
    
    private String buildDeviceInfo() {
        return String.format("Android %s, %s %s, SDK %d", 
            Build.VERSION.RELEASE,
            Build.MANUFACTURER,
            Build.MODEL,
            Build.VERSION.SDK_INT
        );
    }
    
    private String getAppVersion() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "unknown";
        }
    }
    
    // Convenience methods for common actions
    public void logScreenView(String screenName) {
        logAction("SCREEN_VIEW", screenName, "User navigated to screen");
    }
    
    public void logButtonClick(String screenName, String buttonName) {
        logAction("BUTTON_CLICK", screenName, "Clicked: " + buttonName);
    }
    
    public void logApiCall(String screenName, String endpoint, String method) {
        logAction("API_CALL", screenName, method + " " + endpoint);
    }
    
    public void logApiSuccess(String screenName, String endpoint, String method) {
        logAction("API_SUCCESS", screenName, method + " " + endpoint + " completed successfully");
    }
    
    public void logApiError(String screenName, String endpoint, String method, String error) {
        logError("API_ERROR", screenName, method + " " + endpoint, error);
    }
    
    public void logUserAction(String screenName, String action, String details) {
        logAction("USER_ACTION", screenName, action + ": " + details);
    }
    
    public void logAppEvent(String event, String details) {
        logAction("APP_EVENT", "SYSTEM", event + ": " + details);
    }
} 