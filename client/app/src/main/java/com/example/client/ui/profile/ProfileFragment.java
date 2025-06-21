package com.example.client.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvProfileStatus;
    private LinearLayout layoutProfileForm;
    private LinearLayout layoutProfileDisplay;
    private EditText etFormation, etPreferences;
    private ChipGroup chipGroupSkills;
    private Button btnSaveProfile, btnEditProfile;
    private ProgressBar progressBar;
    
    private ApiClient apiClient;
    private DebugLogger debugLogger;
    private ApiModels.Profile currentProfile;
    private boolean isEditing = false;

    // Common skills for binome matching
    private String[] availableSkills = {
        "Java", "Python", "JavaScript", "C++", "C#", "React", "Angular", "Vue.js",
        "Node.js", "Spring Boot", "Android", "iOS", "Flutter", "Database", "SQL",
        "MongoDB", "Machine Learning", "AI", "Web Development", "Mobile Development",
        "DevOps", "Git", "Docker", "Kubernetes", "AWS", "Azure", "UI/UX Design",
        "Data Science", "Cybersecurity", "Blockchain", "Game Development"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        debugLogger.logScreenView("PROFILE");
        debugLogger.logAction("LIFECYCLE", "PROFILE", "ProfileFragment onCreateView started");
        
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(root);
        apiClient = ApiClient.getInstance(requireContext());
        debugLogger.logAppEvent("API_INIT", "ApiClient instance retrieved in ProfileFragment");
        
        setupSkillChips();
        setupClickListeners();
        loadProfile();
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLogger.logScreenView("PROFILE");
    }

    private void initViews(View root) {
        debugLogger.logAction("UI_INIT", "PROFILE", "Initializing profile fragment views");
        tvProfileStatus = root.findViewById(R.id.tvProfileStatus);
        layoutProfileForm = root.findViewById(R.id.layoutProfileForm);
        layoutProfileDisplay = root.findViewById(R.id.layoutProfileDisplay);
        etFormation = root.findViewById(R.id.etFormation);
        etPreferences = root.findViewById(R.id.etPreferences);
        chipGroupSkills = root.findViewById(R.id.chipGroupSkills);
        btnSaveProfile = root.findViewById(R.id.btnSaveProfile);
        btnEditProfile = root.findViewById(R.id.btnEditProfile);
        progressBar = root.findViewById(R.id.progressBar);
    }

    private void setupSkillChips() {
        debugLogger.logAction("UI_SETUP", "PROFILE", "Setting up " + availableSkills.length + " skill chips");
        chipGroupSkills.removeAllViews();
        
        for (String skill : availableSkills) {
            Chip chip = new Chip(requireContext());
            chip.setText(skill);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                debugLogger.logUserAction("PROFILE", "SKILL_TOGGLE", 
                    skill + " " + (isChecked ? "selected" : "deselected"));
            });
            chipGroupSkills.addView(chip);
        }
    }

    private void setupClickListeners() {
        debugLogger.logAction("UI_SETUP", "PROFILE", "Setting up click listeners");
        btnSaveProfile.setOnClickListener(v -> {
            debugLogger.logButtonClick("PROFILE", "SAVE_PROFILE");
            saveProfile();
        });
        btnEditProfile.setOnClickListener(v -> {
            debugLogger.logButtonClick("PROFILE", "EDIT_PROFILE");
            enableEditing();
        });
    }

    private void loadProfile() {
        debugLogger.logApiCall("PROFILE", "/api/profile/current", "GET");
        setLoading(true);
        
        Call<ApiModels.Profile> call = apiClient.getApiService().getCurrentProfile();
        call.enqueue(new Callback<ApiModels.Profile>() {
            @Override
            public void onResponse(Call<ApiModels.Profile> call, Response<ApiModels.Profile> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("PROFILE", "/api/profile/current", "GET");
                    currentProfile = response.body();
                    debugLogger.logAction("PROFILE_DATA", "PROFILE", "Profile loaded: " + 
                        currentProfile.getFormation() + ", " + 
                        (currentProfile.getSkills() != null ? currentProfile.getSkills().size() : 0) + " skills");
                    displayProfile(currentProfile);
                } else {
                    debugLogger.logAction("PROFILE_DATA", "PROFILE", "No profile found, showing creation form");
                    // No profile exists, show creation form
                    showProfileCreationForm();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.Profile> call, Throwable t) {
                setLoading(false);
                debugLogger.logApiError("PROFILE", "/api/profile/current", "GET", "Load failed: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for profile loading");
                    // Enable mock mode and retry
                    apiClient.enableMockMode(true);
                    Toast.makeText(requireContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    loadProfile(); // Retry with mock service
                } else {
                    Toast.makeText(requireContext(), "Error loading profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    showProfileCreationForm();
                }
            }
        });
    }

    private void showProfileCreationForm() {
        debugLogger.logAction("UI_STATE", "PROFILE", "Showing profile creation form");
        tvProfileStatus.setText(getString(R.string.profile_not_created));
        layoutProfileDisplay.setVisibility(View.GONE);
        layoutProfileForm.setVisibility(View.VISIBLE);
        btnSaveProfile.setText(getString(R.string.create_profile));
        btnEditProfile.setVisibility(View.GONE);
        isEditing = true;
    }

    private void displayProfile(ApiModels.Profile profile) {
        debugLogger.logAction("UI_STATE", "PROFILE", "Displaying existing profile");
        tvProfileStatus.setText("Profile Completed ✓");
        layoutProfileDisplay.setVisibility(View.VISIBLE);
        layoutProfileForm.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.VISIBLE);
        isEditing = false;

        // Display profile information in read-only mode
        TextView tvFormationDisplay = layoutProfileDisplay.findViewById(R.id.tvFormationDisplay);
        TextView tvSkillsDisplay = layoutProfileDisplay.findViewById(R.id.tvSkillsDisplay);
        TextView tvPreferencesDisplay = layoutProfileDisplay.findViewById(R.id.tvPreferencesDisplay);

        tvFormationDisplay.setText("Formation: " + profile.getFormation());
        
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            StringBuilder skillsText = new StringBuilder("Skills: ");
            for (String skill : profile.getSkills()) {
                skillsText.append(skill).append(", ");
            }
            // Remove last comma
            if (skillsText.length() > 8) {
                skillsText.setLength(skillsText.length() - 2);
            }
            tvSkillsDisplay.setText(skillsText.toString());
        } else {
            tvSkillsDisplay.setText("Skills: None specified");
        }

        String preferences = profile.getPreferences();
        tvPreferencesDisplay.setText("Preferences: " + (preferences != null && !preferences.isEmpty() ? preferences : "None specified"));
    }

    private void enableEditing() {
        debugLogger.logUserAction("PROFILE", "EDIT_MODE", "User entered edit mode");
        layoutProfileDisplay.setVisibility(View.GONE);
        layoutProfileForm.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.GONE);
        btnSaveProfile.setText("Update Profile");
        isEditing = true;

        // Pre-fill form with existing data
        if (currentProfile != null) {
            etFormation.setText(currentProfile.getFormation());
            etPreferences.setText(currentProfile.getPreferences());
            
            // Select existing skills
            if (currentProfile.getSkills() != null) {
                debugLogger.logAction("UI_STATE", "PROFILE", "Pre-filling form with " + 
                    currentProfile.getSkills().size() + " existing skills");
                for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupSkills.getChildAt(i);
                    chip.setChecked(currentProfile.getSkills().contains(chip.getText().toString()));
                }
            }
        }
    }

    private void saveProfile() {
        String formation = etFormation.getText().toString().trim();
        String preferences = etPreferences.getText().toString().trim();
        
        debugLogger.logUserAction("PROFILE", "SAVE_ATTEMPT", "Formation: " + formation + 
            ", Preferences length: " + preferences.length());
        
        if (formation.isEmpty()) {
            debugLogger.logError("PROFILE", "SAVE", "Validation failed", "Formation field is empty");
            Toast.makeText(requireContext(), "Please enter your formation", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected skills
        Set<String> selectedSkills = new HashSet<>();
        for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSkills.getChildAt(i);
            if (chip.isChecked()) {
                selectedSkills.add(chip.getText().toString());
            }
        }
        
        debugLogger.logAction("PROFILE_DATA", "PROFILE", "Selected " + selectedSkills.size() + " skills");

        // Create profile request
        ApiModels.CreateProfileRequest request = new ApiModels.CreateProfileRequest(formation, selectedSkills, preferences);
        
        setLoading(true);
        debugLogger.logApiCall("PROFILE", "/api/profile", isEditing ? "PUT" : "POST");
        
        Call<ApiModels.Profile> call = apiClient.getApiService().createProfile(request);
            
        call.enqueue(new Callback<ApiModels.Profile>() {
            @Override
            public void onResponse(Call<ApiModels.Profile> call, Response<ApiModels.Profile> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("PROFILE", "/api/profile", isEditing ? "PUT" : "POST");
                    debugLogger.logAppEvent("PROFILE_SUCCESS", "Profile " + (isEditing ? "updated" : "created") + " successfully");
                    
                    currentProfile = response.body();
                    displayProfile(currentProfile);
                    Toast.makeText(requireContext(), 
                        "Profile " + (isEditing ? "updated" : "created") + " successfully!", 
                        Toast.LENGTH_SHORT).show();
                } else {
                    debugLogger.logApiError("PROFILE", "/api/profile", isEditing ? "PUT" : "POST", 
                        "Response code: " + response.code());
                    Toast.makeText(requireContext(), 
                        "Failed to " + (isEditing ? "update" : "create") + " profile. Try again.", 
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.Profile> call, Throwable t) {
                setLoading(false);
                debugLogger.logApiError("PROFILE", "/api/profile", isEditing ? "PUT" : "POST", 
                    "Network failure: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for profile save");
                    // Enable mock mode and retry
                    apiClient.enableMockMode(true);
                    Toast.makeText(requireContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    saveProfile(); // Retry with mock service
                } else {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!loading);
        btnEditProfile.setEnabled(!loading);
        etFormation.setEnabled(!loading);
        etPreferences.setEnabled(!loading);
        
        for (int i = 0; i < chipGroupSkills.getChildCount(); i++) {
            chipGroupSkills.getChildAt(i).setEnabled(!loading);
        }
    }

    private boolean isNetworkError(Throwable t) {
        // Implement your logic to determine if the error is due to network connectivity
        return t instanceof java.net.ConnectException || t instanceof java.net.SocketTimeoutException;
    }
} 