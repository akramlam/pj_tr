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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
// import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.BinomeApiService;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class MatchesFragment extends Fragment {
    
    private RecyclerView recyclerView;
    // private SwipeRefreshLayout swipeRefreshLayout;
    private MatchesAdapter adapter;
    private MaterialCardView emptyStateCard;
    private MaterialButton completeProfileButton;
    private BinomeApiService apiService;
    private DebugLogger debugLogger;
    private List<ApiModels.PotentialMatch> potentialMatches = new ArrayList<>();
    
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
        setupRecyclerView();
        setupButtons();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        debugLogger.logAppEvent("API_INIT", "ApiService instance retrieved in MatchesFragment");
        loadMatches();
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLogger.logScreenView("MATCHES");
    }
    
    private void initializeViews(View view) {
        debugLogger.logAction("UI_INIT", "MATCHES", "Initializing matches fragment views");
        recyclerView = view.findViewById(R.id.matches_recycler_view);
        // swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_matches);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        completeProfileButton = view.findViewById(R.id.complete_profile_button);
    }
    
    private void setupRecyclerView() {
        debugLogger.logAction("UI_SETUP", "MATCHES", "Setting up RecyclerView for matches");
        adapter = new MatchesAdapter(potentialMatches, this::onMatchAction);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
    
    private void setupButtons() {
        debugLogger.logAction("UI_SETUP", "MATCHES", "Setting up button click listeners");
        completeProfileButton.setOnClickListener(v -> {
            debugLogger.logButtonClick("MATCHES", "COMPLETE_PROFILE");
            debugLogger.logUserAction("MATCHES", "NAVIGATION", "User clicked complete profile button");
            // Navigate to profile fragment
            Navigation.findNavController(v).navigate(R.id.nav_profile);
        });
    }
    
    // Temporarily disabled SwipeRefreshLayout due to dependency issues
    // private void setupSwipeRefresh() {
    //     swipeRefreshLayout.setOnRefreshListener(this::loadMatches);
    //     swipeRefreshLayout.setColorSchemeResources(R.color.purple_500);
    // }
    
    private void loadMatches() {
        debugLogger.logApiCall("MATCHES", "/api/matches/potential", "GET");
        // swipeRefreshLayout.setRefreshing(true);
        
        apiService.getPotentialMatches().enqueue(new Callback<List<ApiModels.PotentialMatch>>() {
            @Override
            public void onResponse(Call<List<ApiModels.PotentialMatch>> call, Response<List<ApiModels.PotentialMatch>> response) {
                // swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    debugLogger.logApiSuccess("MATCHES", "/api/matches/potential", "GET");
                    debugLogger.logAction("MATCHES_DATA", "MATCHES", "Loaded " + response.body().size() + " potential matches");
                    
                    potentialMatches.clear();
                    potentialMatches.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    
                    updateEmptyState();
                } else {
                    debugLogger.logApiError("MATCHES", "/api/matches/potential", "GET", "Response code: " + response.code());
                    Toast.makeText(getContext(), "Failed to load matches: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ApiModels.PotentialMatch>> call, Throwable t) {
                // swipeRefreshLayout.setRefreshing(false);
                debugLogger.logApiError("MATCHES", "/api/matches/potential", "GET", "Network failure: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for matches loading");
                    // Enable mock mode and retry
                    ApiClient.getInstance(getContext()).enableMockMode(true);
                    Toast.makeText(getContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    loadMatches(); // Retry with mock service
                } else {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void updateEmptyState() {
        if (potentialMatches.isEmpty()) {
            debugLogger.logAction("UI_STATE", "MATCHES", "Showing empty state - no matches available");
            emptyStateCard.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            debugLogger.logAction("UI_STATE", "MATCHES", "Showing matches list with " + potentialMatches.size() + " items");
            emptyStateCard.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void onMatchAction(ApiModels.PotentialMatch match, boolean isLike) {
        String action = isLike ? "LIKE" : "PASS";
        debugLogger.logUserAction("MATCHES", "MATCH_ACTION", action + " for user: " + match.userId);
        debugLogger.logApiCall("MATCHES", "/api/matches/action", "POST");
        
        ApiModels.MatchRequest request = new ApiModels.MatchRequest();
        request.targetUserId = match.userId;
        request.action = action;
        
        apiService.sendMatchRequest(request).enqueue(new Callback<ApiModels.MatchResponse>() {
            @Override
            public void onResponse(Call<ApiModels.MatchResponse> call, Response<ApiModels.MatchResponse> response) {
                if (response.isSuccessful()) {
                    debugLogger.logApiSuccess("MATCHES", "/api/matches/action", "POST");
                    
                    potentialMatches.remove(match);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    
                    if (isLike && response.body() != null && response.body().isMatch) {
                        debugLogger.logAppEvent("MATCH_SUCCESS", "Mutual match created with user: " + match.userId);
                        Toast.makeText(getContext(), "It's a match! 🎉", Toast.LENGTH_LONG).show();
                    } else {
                        debugLogger.logAction("MATCH_RESULT", "MATCHES", action + " action completed for user: " + match.userId);
                    }
                } else {
                    debugLogger.logApiError("MATCHES", "/api/matches/action", "POST", "Response code: " + response.code());
                    Toast.makeText(getContext(), "Action failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiModels.MatchResponse> call, Throwable t) {
                debugLogger.logApiError("MATCHES", "/api/matches/action", "POST", "Network failure: " + t.getMessage());
                
                // Check if this is a network connectivity issue
                if (isNetworkError(t)) {
                    debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for match action");
                    // Enable mock mode and retry
                    ApiClient.getInstance(getContext()).enableMockMode(true);
                    Toast.makeText(getContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                    onMatchAction(match, isLike); // Retry with mock service
                } else {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    public interface OnMatchActionListener {
        void onMatchAction(ApiModels.PotentialMatch match, boolean isLike);
    }
    
    private boolean isNetworkError(Throwable t) {
        return t instanceof java.net.ConnectException || 
               t instanceof java.net.SocketTimeoutException ||
               t instanceof java.io.IOException;
    }
}
