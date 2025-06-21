package com.example.client.ui.matches;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectDetailFragment extends Fragment {
    
    private MaterialToolbar toolbar;
    private MaterialTextView projectTitle;
    private MaterialTextView projectDescription;
    private MaterialTextView projectCategory;
    private MaterialTextView projectDifficulty;
    private MaterialTextView projectDuration;
    private MaterialTextView projectCreator;
    private MaterialTextView teamInfo;
    private MaterialTextView compatibilityScore;
    private CircularProgressIndicator compatibilityIndicator;
    private ChipGroup skillsChipGroup;
    private MaterialButton joinProjectButton;
    private MaterialButton passProjectButton;
    private MaterialButton contactCreatorButton;
    
    private ApiModels.PotentialMatch projectMatch;
    private DebugLogger debugLogger;
    private ApiClient apiClient;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        
        // Get project data from arguments
        if (getArguments() != null) {
            Long projectId = getArguments().getLong("projectId", -1);
            String projectTitle = getArguments().getString("projectTitle", "");
            String projectDescription = getArguments().getString("projectDescription", "");
            String category = getArguments().getString("category", "");
            String difficulty = getArguments().getString("difficulty", "");
            String duration = getArguments().getString("duration", "");
            String createdBy = getArguments().getString("createdBy", "");
            int teamSize = getArguments().getInt("teamSize", 1);
            int currentTeamSize = getArguments().getInt("currentTeamSize", 0);
            int compatibilityScore = getArguments().getInt("compatibilityScore", 0);
            
            // Reconstruct PotentialMatch object
            projectMatch = new ApiModels.PotentialMatch();
            projectMatch.setProjectId(projectId);
            projectMatch.setProjectTitle(projectTitle);
            projectMatch.setProjectDescription(projectDescription);
            projectMatch.setCategory(category);
            projectMatch.setDifficulty(difficulty);
            projectMatch.setDuration(duration);
            projectMatch.setCreatedBy(createdBy);
            projectMatch.setTeamSize(teamSize);
            projectMatch.setCurrentTeamSize(currentTeamSize);
            projectMatch.setCompatibilityScore(compatibilityScore);
            
            // Get skills array
            String[] skillsArray = getArguments().getStringArray("matchingSkills");
            if (skillsArray != null) {
                java.util.Set<String> skills = new java.util.HashSet<>();
                for (String skill : skillsArray) {
                    skills.add(skill);
                }
                projectMatch.setMatchingSkills(skills);
            }
        }
        
        debugLogger.logScreenView("PROJECT_DETAIL");
        debugLogger.logAction("LIFECYCLE", "PROJECT_DETAIL", "ProjectDetailFragment onCreateView started");
        
        return inflater.inflate(R.layout.fragment_project_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        apiClient = ApiClient.getInstance(requireContext());
        
        initializeViews(view);
        setupToolbar();
        setupButtons();
        
        if (projectMatch != null) {
            displayProjectDetails();
        }
    }
    
    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.project_detail_toolbar);
        projectTitle = view.findViewById(R.id.project_title);
        projectDescription = view.findViewById(R.id.project_description);
        projectCategory = view.findViewById(R.id.project_category);
        projectDifficulty = view.findViewById(R.id.project_difficulty);
        projectDuration = view.findViewById(R.id.project_duration);
        projectCreator = view.findViewById(R.id.project_creator);
        teamInfo = view.findViewById(R.id.team_info);
        compatibilityScore = view.findViewById(R.id.compatibility_score);
        compatibilityIndicator = view.findViewById(R.id.compatibility_indicator);
        skillsChipGroup = view.findViewById(R.id.skills_chip_group);
        joinProjectButton = view.findViewById(R.id.join_project_button);
        passProjectButton = view.findViewById(R.id.pass_project_button);
        contactCreatorButton = view.findViewById(R.id.contact_creator_button);
    }
    
    private void setupToolbar() {
        if (toolbar != null) {
            toolbar.setTitle("Project Details");
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> {
                debugLogger.logUserAction("PROJECT_DETAIL", "NAVIGATION", "User clicked back button");
                requireActivity().onBackPressed();
            });
        }
    }
    
    private void setupButtons() {
        joinProjectButton.setOnClickListener(v -> {
            debugLogger.logUserAction("PROJECT_DETAIL", "ACTION", "User clicked join project");
            handleJoinProject();
        });
        
        passProjectButton.setOnClickListener(v -> {
            debugLogger.logUserAction("PROJECT_DETAIL", "ACTION", "User clicked pass project");
            handlePassProject();
        });
        
        contactCreatorButton.setOnClickListener(v -> {
            debugLogger.logUserAction("PROJECT_DETAIL", "ACTION", "User clicked contact creator");
            handleContactCreator();
        });
    }
    
    private void displayProjectDetails() {
        if (projectMatch == null) return;
        
        projectTitle.setText(projectMatch.getProjectTitle());
        projectDescription.setText(projectMatch.getProjectDescription());
        projectCategory.setText(projectMatch.getCategory());
        projectDifficulty.setText(projectMatch.getDifficulty());
        projectDuration.setText(projectMatch.getDuration());
        projectCreator.setText("Created by " + projectMatch.getCreatedBy());
        
        // Team information
        int openSpots = projectMatch.getTeamSize() - projectMatch.getCurrentTeamSize();
        teamInfo.setText(String.format("Team: %d/%d members (%d spots available)", 
            projectMatch.getCurrentTeamSize(), 
            projectMatch.getTeamSize(), 
            openSpots));
        
        // Compatibility
        int compatibility = projectMatch.getCompatibilityScore();
        compatibilityScore.setText(compatibility + "% Match");
        compatibilityIndicator.setProgress(compatibility);
        
        // Set compatibility color based on score
        int color;
        if (compatibility >= 80) {
            color = ContextCompat.getColor(requireContext(), R.color.success_green);
        } else if (compatibility >= 60) {
            color = ContextCompat.getColor(requireContext(), R.color.warning_orange);
        } else {
            color = ContextCompat.getColor(requireContext(), R.color.error_red);
        }
        compatibilityScore.setTextColor(color);
        compatibilityIndicator.setIndicatorColor(color);
        
        // Skills
        skillsChipGroup.removeAllViews();
        if (projectMatch.getMatchingSkills() != null && !projectMatch.getMatchingSkills().isEmpty()) {
            for (String skill : projectMatch.getMatchingSkills()) {
                Chip chip = new Chip(requireContext());
                chip.setText(skill);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.purple_100);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700));
                skillsChipGroup.addView(chip);
            }
        }
        
        // Enable/disable join button based on team availability
        if (openSpots <= 0) {
            joinProjectButton.setEnabled(false);
            joinProjectButton.setText("Team Full");
        }
    }
    
    private void handleJoinProject() {
        if (projectMatch == null) return;
        
        // Create match request
        ApiModels.MatchRequest request = new ApiModels.MatchRequest();
        request.action = "LIKE";
        request.targetProjectId = projectMatch.getProjectId();
        
        joinProjectButton.setEnabled(false);
        joinProjectButton.setText("Joining...");
        
        apiClient.getApiService().sendMatchAction(request).enqueue(new Callback<ApiModels.MatchResponse>() {
            @Override
            public void onResponse(Call<ApiModels.MatchResponse> call, Response<ApiModels.MatchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("PROJECT_DETAIL", "/api/matches/action", "POST");
                    
                    ApiModels.MatchResponse matchResponse = response.body();
                    if (matchResponse.isMatch()) {
                        Toast.makeText(requireContext(), "🎉 Great! You've joined the project!", Toast.LENGTH_LONG).show();
                        requireActivity().onBackPressed(); // Go back to matches
                    } else {
                        Toast.makeText(requireContext(), "Request sent to project creator!", Toast.LENGTH_LONG).show();
                        joinProjectButton.setText("Request Sent");
                    }
                } else {
                    debugLogger.logApiError("PROJECT_DETAIL", "/api/matches/action", "POST", "Response code: " + response.code());
                    Toast.makeText(requireContext(), "Failed to join project. Please try again.", Toast.LENGTH_SHORT).show();
                    joinProjectButton.setEnabled(true);
                    joinProjectButton.setText("Join Project");
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.MatchResponse> call, Throwable t) {
                debugLogger.logApiError("PROJECT_DETAIL", "/api/matches/action", "POST", "Network error: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                joinProjectButton.setEnabled(true);
                joinProjectButton.setText("Join Project");
            }
        });
    }
    
    private void handlePassProject() {
        if (projectMatch == null) return;
        
        // Create match request
        ApiModels.MatchRequest request = new ApiModels.MatchRequest();
        request.action = "PASS";
        request.targetProjectId = projectMatch.getProjectId();
        
        apiClient.getApiService().sendMatchAction(request).enqueue(new Callback<ApiModels.MatchResponse>() {
            @Override
            public void onResponse(Call<ApiModels.MatchResponse> call, Response<ApiModels.MatchResponse> response) {
                if (response.isSuccessful()) {
                    debugLogger.logApiSuccess("PROJECT_DETAIL", "/api/matches/action", "POST");
                    Toast.makeText(requireContext(), "Project passed", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // Go back to matches
                } else {
                    debugLogger.logApiError("PROJECT_DETAIL", "/api/matches/action", "POST", "Response code: " + response.code());
                    Toast.makeText(requireContext(), "Failed to pass project", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.MatchResponse> call, Throwable t) {
                debugLogger.logApiError("PROJECT_DETAIL", "/api/matches/action", "POST", "Network error: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleContactCreator() {
        if (projectMatch == null) return;
        
        // Navigate to conversation with project creator
        Bundle args = new Bundle();
        args.putString("username", projectMatch.getCreatedBy());
        Navigation.findNavController(requireView()).navigate(R.id.nav_conversation_detail, args);
    }
} 