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
import com.example.client.databinding.FragmentHomeBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ApiClient apiClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        android.util.Log.d("HomeFragment", "onCreateView started");
        
        try {
            binding = FragmentHomeBinding.inflate(inflater, container, false);
            View root = binding.getRoot();

            android.util.Log.d("HomeFragment", "View binding inflated successfully");

            // Initialize text views with simple text first
            binding.textWelcome.setText("Welcome to Binome Matcher!");
            binding.textProfileStatus.setText("Profile status: Ready");
            binding.textProfileInfo.setText("Profile info: Loading...");
            binding.textMatchingStatus.setText("Matching status: Ready");

            // Simple click listeners without navigation for now
            binding.btnCreateProfile.setOnClickListener(v -> 
                Toast.makeText(requireContext(), "Profile button clicked!", Toast.LENGTH_SHORT).show());
            
            binding.btnFindMatches.setOnClickListener(v -> 
                Toast.makeText(requireContext(), "Matches button clicked!", Toast.LENGTH_SHORT).show());
            
            binding.btnViewMessages.setOnClickListener(v -> 
                Toast.makeText(requireContext(), "Messages button clicked!", Toast.LENGTH_SHORT).show());

            android.util.Log.d("HomeFragment", "onCreateView completed successfully");
            return root;
            
        } catch (Exception e) {
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}