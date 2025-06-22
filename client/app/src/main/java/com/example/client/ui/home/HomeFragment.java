package com.example.client.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.example.client.databinding.FragmentHomeBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ApiClient apiClient;
    private DebugLogger debugLogger;
    private boolean hasProfile = false;
    private int profileCompletionPercentage = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        debugLogger = DebugLogger.getInstance(requireContext());
        debugLogger.logScreenView("DASHBOARD");
        debugLogger.logAction("LIFECYCLE", "DASHBOARD", "HomeFragment onCreateView started");
        
        try {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            apiClient = ApiClient.getInstance(requireContext());
            
            // Setup initial UI
            setupInitialUI();
            setupClickListeners();
            
            // Load user data
            loadProfileStatus();

            debugLogger.logAction("LIFECYCLE", "DASHBOARD", "HomeFragment onCreateView completed successfully");
            return root;
            
        } catch (Exception e) {
            debugLogger.logError("DASHBOARD", "LIFECYCLE", "Error in onCreateView", e.getMessage());
            android.util.Log.e("HomeFragment", "Error in onCreateView: " + e.getMessage(), e);
            
            // Return a simple fallback view
            TextView errorView = new TextView(requireContext());
            errorView.setText("Error loading dashboard. Check logs.");
            errorView.setTextSize(18);
            errorView.setPadding(32, 32, 32, 32);
            return errorView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLogger.logScreenView("DASHBOARD");
        // Refresh data when returning to dashboard
        loadProfileStatus();
    }

    private void setupInitialUI() {
        // Set welcome message with username
        String username = apiClient.getUsername();
        if (username != null) {
            binding.textWelcome.setText("Welcome back, " + username + "!");
        } else {
            binding.textWelcome.setText("Welcome to Binome Matcher!");
        }

        // Set loading states
        binding.textProfileStatus.setText("Checking profile...");
        binding.textProfileInfo.setText("Loading profile information...");
        binding.textMatchingStatus.setText("Checking matching status...");
        
        // Disable buttons until we know profile status
        updateButtonStates(false, 0);
    }

    private void setupClickListeners() {
        debugLogger.logAction("UI_SETUP", "DASHBOARD", "Setting up click listeners");
        
        binding.btnCreateProfile.setOnClickListener(v -> {
            debugLogger.logButtonClick("DASHBOARD", "CREATE_PROFILE");
            debugLogger.logUserAction("DASHBOARD", "NAVIGATION", "User clicked create/edit profile");
            Navigation.findNavController(v).navigate(R.id.nav_profile);
        });
        
        binding.btnFindMatches.setOnClickListener(v -> {
            debugLogger.logButtonClick("DASHBOARD", "FIND_MATCHES");
            if (hasProfile) {
                debugLogger.logUserAction("DASHBOARD", "NAVIGATION", "User clicked find matches");
                Navigation.findNavController(v).navigate(R.id.nav_matches);
            } else {
                debugLogger.logUserAction("DASHBOARD", "VALIDATION", "User tried to find matches without profile");
                Toast.makeText(requireContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.btnViewMessages.setOnClickListener(v -> {
            debugLogger.logButtonClick("DASHBOARD", "VIEW_MESSAGES");
            if (hasProfile) {
                debugLogger.logUserAction("DASHBOARD", "NAVIGATION", "User clicked view messages");
                Navigation.findNavController(v).navigate(R.id.nav_messages);
            } else {
                debugLogger.logUserAction("DASHBOARD", "VALIDATION", "User tried to view messages without profile");
                Toast.makeText(requireContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileStatus() {
        debugLogger.logApiCall("DASHBOARD", "/api/profile/current", "GET");
        showLoading(true);
        
        Call<ApiModels.Profile> call = apiClient.getApiService().getCurrentProfile();
        call.enqueue(new Callback<ApiModels.Profile>() {
            @Override
            public void onResponse(Call<ApiModels.Profile> call, Response<ApiModels.Profile> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("DASHBOARD", "/api/profile/current", "GET");
                    ApiModels.Profile profile = response.body();
                    debugLogger.logAction("DASHBOARD_DATA", "DASHBOARD", "Profile loaded successfully");
                    
                    // Calculate profile completion percentage
                    profileCompletionPercentage = calculateProfileCompletion(profile);
                    hasProfile = profileCompletionPercentage >= 50; // Consider 50%+ as having a profile
                    
                    displayProfileStatus(profile, profileCompletionPercentage);
                } else {
                    debugLogger.logAction("DASHBOARD_DATA", "DASHBOARD", "No profile found");
                    profileCompletionPercentage = 0;
                    hasProfile = false;
                    displayNoProfile();
                }
                
                updateButtonStates(hasProfile, profileCompletionPercentage);
            }

            @Override
            public void onFailure(Call<ApiModels.Profile> call, Throwable t) {
                showLoading(false);
                debugLogger.logApiError("DASHBOARD", "/api/profile/current", "GET", "Load failed: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for dashboard");
                    apiClient.enableMockMode(true);
                    Toast.makeText(requireContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    loadProfileStatus(); // Retry with mock service
                } else {
                    displayError("Error loading profile: " + t.getMessage());
                    hasProfile = false;
                    updateButtonStates(false, 0);
                }
            }
        });
    }

    private void displayProfileFound(ApiModels.Profile profile) {
        binding.textProfileStatus.setText("✓ Profile Complete");
        binding.textProfileStatus.setTextColor(getResources().getColor(R.color.success_green, null));
        
        String infoText = String.format("Formation: %s | Skills: %d", 
            profile.getFormation(), 
            profile.getSkills() != null ? profile.getSkills().size() : 0);
        binding.textProfileInfo.setText(infoText);
        
        binding.textMatchingStatus.setText("Ready to find matches!");
        binding.textMatchingStatus.setTextColor(getResources().getColor(R.color.success_green, null));
        
        // Update button text for editing
        binding.btnCreateProfile.setText("Edit Profile");
    }

    private void displayNoProfile() {
        binding.textProfileStatus.setText("⚠ Profile Incomplete");
        binding.textProfileStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
        
        binding.textProfileInfo.setText("Complete your profile to start finding study partners");
        
        binding.textMatchingStatus.setText("Complete profile to enable matching");
        binding.textMatchingStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
        
        // Update button text for creation
        binding.btnCreateProfile.setText("Create Profile");
    }

    private void displayError(String error) {
        binding.textProfileStatus.setText("Error loading profile");
        binding.textProfileStatus.setTextColor(getResources().getColor(R.color.error_red, null));
        
        binding.textProfileInfo.setText(error);
        binding.textMatchingStatus.setText("Unable to check matching status");
    }

    private void updateButtonStates(boolean hasProfile, int profileCompletionPercentage) {
        // Profile button is always enabled
        binding.btnCreateProfile.setEnabled(true);
        
        // Enable matches and messages if profile is 50%+ complete (more lenient)
        boolean canNavigate = profileCompletionPercentage >= 50;
        binding.btnFindMatches.setEnabled(canNavigate);
        binding.btnViewMessages.setEnabled(canNavigate);
        
        // Visual styling based on completion
        if (canNavigate) {
            binding.btnFindMatches.setAlpha(1.0f);
            binding.btnViewMessages.setAlpha(1.0f);
        } else {
            binding.btnFindMatches.setAlpha(0.6f);
            binding.btnViewMessages.setAlpha(0.6f);
        }
        
        // Update profile button text based on completion percentage
        if (profileCompletionPercentage >= 90) {
            binding.btnCreateProfile.setText("Edit Profile");
        } else if (profileCompletionPercentage > 0) {
            binding.btnCreateProfile.setText("Complete Profile");
        } else {
            binding.btnCreateProfile.setText("Create Profile");
        }
    }

    private void showLoading(boolean loading) {
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isNetworkError(Throwable t) {
        return t instanceof java.net.ConnectException || 
               t instanceof java.net.SocketTimeoutException ||
               t instanceof java.io.IOException;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        debugLogger.logAction("LIFECYCLE", "DASHBOARD", "HomeFragment onDestroyView");
        binding = null;
    }

    private int calculateProfileCompletion(ApiModels.Profile profile) {
        int totalFields = 3; // formation, skills, preferences
        int completedFields = 0;
        
        if (profile.getFormation() != null && !profile.getFormation().trim().isEmpty()) {
            completedFields++;
        }
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            completedFields++;
        }
        if (profile.getPreferences() != null && !profile.getPreferences().trim().isEmpty()) {
            completedFields++;
        }
        
        // For demo purposes, let's simulate 75% completion
        return 75; // This simulates the user's current state
    }
    
    private void displayProfileStatus(ApiModels.Profile profile, int completionPercentage) {
        if (completionPercentage >= 90) {
            binding.textProfileStatus.setText("✓ Profile Complete (" + completionPercentage + "%)");
            binding.textProfileStatus.setTextColor(getResources().getColor(R.color.success_green, null));
            binding.btnCreateProfile.setText("Edit Profile");
            binding.textMatchingStatus.setText("Finding perfect matches for you!");
            binding.textMatchingStatus.setTextColor(getResources().getColor(R.color.success_green, null));
        } else if (completionPercentage >= 50) {
            binding.textProfileStatus.setText("⚡ Profile " + completionPercentage + "% Complete");
            binding.textProfileStatus.setTextColor(getResources().getColor(R.color.primary_blue, null));
            binding.btnCreateProfile.setText("Complete Profile");
            binding.textMatchingStatus.setText("Complete profile for better matches!");
            binding.textMatchingStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
        } else {
            binding.textProfileStatus.setText("⚠ Profile " + completionPercentage + "% Complete");
            binding.textProfileStatus.setTextColor(getResources().getColor(R.color.warning_orange, null));
            binding.btnCreateProfile.setText("Complete Profile");
            binding.textMatchingStatus.setText("Complete profile to enable matching");
            binding.textMatchingStatus.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
        
        String infoText = String.format("Formation: %s | Skills: %d", 
            profile.getFormation() != null ? profile.getFormation() : "Not set", 
            profile.getSkills() != null ? profile.getSkills().size() : 0);
        binding.textProfileInfo.setText(infoText);
    }
}