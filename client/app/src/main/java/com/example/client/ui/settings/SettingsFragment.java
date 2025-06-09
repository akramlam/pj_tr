package com.example.client.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.client.R;
import com.example.client.LoginActivity;
import com.example.client.api.ApiClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {
    
    private MaterialTextView usernameText;
    private MaterialTextView emailText;
    private SwitchMaterial notificationsSwitch;
    private SwitchMaterial darkModeSwitch;
    private SwitchMaterial mockModeSwitch;
    private MaterialButton logoutButton;
    private MaterialButton aboutButton;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupUserInfo();
        setupClickListeners();
    }
    
    private void initializeViews(View view) {
        usernameText = view.findViewById(R.id.username_text);
        emailText = view.findViewById(R.id.email_text);
        notificationsSwitch = view.findViewById(R.id.notifications_switch);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        mockModeSwitch = view.findViewById(R.id.mock_mode_switch);
        logoutButton = view.findViewById(R.id.logout_button);
        aboutButton = view.findViewById(R.id.about_button);
    }
    
    private void setupUserInfo() {
        ApiClient apiClient = ApiClient.getInstance(getContext());
        String username = apiClient.getUsername();
        
        usernameText.setText(username != null ? username : "Unknown User");
        emailText.setText("Email not available");
    }
    
    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> showLogoutDialog());
        aboutButton.setOnClickListener(v -> showAboutDialog());
        
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getContext().getSharedPreferences("BinomePrefs", getContext().MODE_PRIVATE)
                    .edit()
                    .putBoolean("notifications_enabled", isChecked)
                    .apply();
            
            Toast.makeText(getContext(), 
                    isChecked ? "Notifications enabled" : "Notifications disabled", 
                    Toast.LENGTH_SHORT).show();
        });
        
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            getContext().getSharedPreferences("BinomePrefs", getContext().MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode_enabled", isChecked)
                    .apply();
            
            Toast.makeText(getContext(), 
                    isChecked ? "Dark mode enabled (restart required)" : "Light mode enabled", 
                    Toast.LENGTH_SHORT).show();
        });
        
        mockModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ApiClient apiClient = ApiClient.getInstance(getContext());
            apiClient.enableMockMode(isChecked);
            
            Toast.makeText(getContext(), 
                    isChecked ? "Offline mode enabled" : "Offline mode disabled", 
                    Toast.LENGTH_SHORT).show();
        });
        
        loadPreferences();
    }
    
    private void loadPreferences() {
        boolean notificationsEnabled = getContext().getSharedPreferences("BinomePrefs", getContext().MODE_PRIVATE)
                .getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = getContext().getSharedPreferences("BinomePrefs", getContext().MODE_PRIVATE)
                .getBoolean("dark_mode_enabled", false);
        boolean mockModeEnabled = ApiClient.getInstance(getContext()).isMockMode();
        
        notificationsSwitch.setChecked(notificationsEnabled);
        darkModeSwitch.setChecked(darkModeEnabled);
        mockModeSwitch.setChecked(mockModeEnabled);
    }
    
    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        ApiClient.getInstance(getContext()).logout();
        
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("About Binome Matcher")
                .setMessage("Binome Matcher v1.0\n\n" +
                        "Find your perfect study partner based on shared skills and interests.\n\n" +
                        "Built with love using Android & Spring Boot\n\n" +
                        "© 2024 Binome Matcher Team")
                .setPositiveButton("OK", null)
                .show();
    }
}
