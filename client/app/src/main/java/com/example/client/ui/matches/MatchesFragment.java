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
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.BinomeApiService;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchesFragment extends Fragment {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private MatchesPagerAdapter pagerAdapter;
    private MaterialCardView emptyStateCard;
    private MaterialButton completeProfileButton;
    private BinomeApiService apiService;
    private DebugLogger debugLogger;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        debugLogger.logScreenView("MATCHES");
        debugLogger.logAction("LIFECYCLE", "MATCHES", "MatchesFragment onCreateView started");
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        debugLogger.logAction("LIFECYCLE", "MATCHES", "MatchesFragment onViewCreated started");
        
        initializeViews(view);
        setupViewPager();
        setupButtons();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        debugLogger.logAppEvent("API_INIT", "ApiService instance retrieved in MatchesFragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLogger.logScreenView("MATCHES");
    }
    
    private void initializeViews(View view) {
        debugLogger.logAction("UI_INIT", "MATCHES", "Initializing matches fragment views");
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        completeProfileButton = view.findViewById(R.id.complete_profile_button);
    }
    
    private void setupViewPager() {
        debugLogger.logAction("UI_SETUP", "MATCHES", "Setting up ViewPager with tabs");
        
        pagerAdapter = new MatchesPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Study Partners");
                    tab.setIcon(R.drawable.ic_people);
                    break;
                case 1:
                    tab.setText("Open Projects");
                    tab.setIcon(R.drawable.ic_work);
                    break;
                case 2:
                    tab.setText("Recommendations");
                    tab.setIcon(R.drawable.ic_dashboard);
                    break;
            }
        }).attach();
    }
    
    private void setupButtons() {
        debugLogger.logAction("UI_SETUP", "MATCHES", "Setting up button click listeners");
        if (completeProfileButton != null) {
            completeProfileButton.setOnClickListener(v -> {
                debugLogger.logButtonClick("MATCHES", "COMPLETE_PROFILE");
                debugLogger.logUserAction("MATCHES", "NAVIGATION", "User clicked complete profile button");
                Navigation.findNavController(v).navigate(R.id.nav_profile);
            });
        }
    }
    
    // Fragment adapter for ViewPager2
    private static class MatchesPagerAdapter extends FragmentStateAdapter {
        
        public MatchesPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return StudyPartnersFragment.newInstance(new ArrayList<>());
                case 1:
                    return OpenProjectsFragment.newInstance(new ArrayList<>());
                case 2:
                    return RecommendationsFragment.newInstance();
                default:
                    return StudyPartnersFragment.newInstance(new ArrayList<>());
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Study Partners, Open Projects, Recommendations
        }
    }
    
    // Inner classes for backward compatibility with fragment references
    public static class PotentialMatch extends ApiModels.PotentialMatch {
        public PotentialMatch(String username, String formation, Set<String> skills, int score) {
            this.username = username;
            this.formation = formation;
            this.commonSkills = skills;
            this.compatibilityScore = score;
        }
    }
    
    public static class Project {
        private String title;
        private String description;
        private String category;
        private String difficulty;
        private String duration;
        private String createdBy;
        private int teamSize;
        private int currentTeamSize;
        
        public Project(String title, String description, String category, String difficulty, String duration, String createdBy, int teamSize, int currentTeamSize) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.difficulty = difficulty;
            this.duration = duration;
            this.createdBy = createdBy;
            this.teamSize = teamSize;
            this.currentTeamSize = currentTeamSize;
        }
        
        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
        public int getCurrentTeamSize() { return currentTeamSize; }
        public void setCurrentTeamSize(int currentTeamSize) { this.currentTeamSize = currentTeamSize; }
    }
    
    // Interface for match actions (needed by MatchesAdapter)
    public interface OnMatchActionListener {
        void onMatchAction(ApiModels.PotentialMatch match, boolean isLike);
    }
}
