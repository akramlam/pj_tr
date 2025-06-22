package com.example.client.ui.matches;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.client.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class RecommendationsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView titleRecommendations;
    private TextView subtitleRecommendations;
    private ChipGroup skillsChipGroup;
    private List<RecommendationItem> recommendations;
    
    public static RecommendationsFragment newInstance() {
        return new RecommendationsFragment();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recommendations, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewRecommendations);
        swipeRefresh = view.findViewById(R.id.swipeRefreshRecommendations);
        titleRecommendations = view.findViewById(R.id.titleRecommendations);
        subtitleRecommendations = view.findViewById(R.id.subtitleRecommendations);
        skillsChipGroup = view.findViewById(R.id.skillsChipGroup);
        
        setupUI();
        setupRecyclerView();
        loadData();
        
        return view;
    }
    
    private void setupUI() {
        titleRecommendations.setText("🎯 Personalized for You");
        subtitleRecommendations.setText("Based on your skills and interests");
        
        // Add skill chips
        addSkillChip("Machine Learning");
        addSkillChip("Python");
        addSkillChip("React");
        addSkillChip("Android");
    }
    
    private void addSkillChip(String skill) {
        Chip chip = new Chip(getContext());
        chip.setText(skill);
        chip.setChipBackgroundColorResource(R.color.purple_100);
        chip.setTextColor(getResources().getColor(R.color.purple_700, null));
        chip.setClickable(false);
        skillsChipGroup.addView(chip);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendations = new ArrayList<>();
        
        // For now, we'll create a simple adapter when we have one
        // TODO: Create RecommendationsAdapter
        
        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            swipeRefresh.setRefreshing(false);
        });
    }
    
    private void loadData() {
        // Create personalized recommendations
        recommendations = new ArrayList<>();
        
        // Add project recommendations
        recommendations.add(new RecommendationItem("PROJECT", "AI-Powered Study Assistant", 
            "Build an AI tutor using your ML skills", "95% Match", "Machine Learning, Python, NLP"));
        recommendations.add(new RecommendationItem("PROJECT", "Smart Campus App", 
            "React Native app for campus navigation", "88% Match", "React Native, Firebase, Maps API"));
        
        // Add study partner recommendations
        recommendations.add(new RecommendationItem("PARTNER", "Alex Chen", 
            "ML Engineer looking for Python projects", "92% Match", "Python, TensorFlow, Computer Vision"));
        recommendations.add(new RecommendationItem("PARTNER", "Maria Rodriguez", 
            "Full-stack developer specializing in React", "85% Match", "React, Node.js, MongoDB"));
        
        // Add learning recommendations
        recommendations.add(new RecommendationItem("LEARNING", "Advanced NLP Course", 
            "Perfect for your ML background", "New", "Natural Language Processing"));
        recommendations.add(new RecommendationItem("LEARNING", "React Native Workshop", 
            "Expand your mobile development skills", "Trending", "Mobile Development"));
    }
    
    // Data class for recommendations
    public static class RecommendationItem {
        private String type; // PROJECT, PARTNER, LEARNING
        private String title;
        private String description;
        private String matchPercentage;
        private String skills;
        
        public RecommendationItem(String type, String title, String description, String matchPercentage, String skills) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.matchPercentage = matchPercentage;
            this.skills = skills;
        }
        
        // Getters
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getMatchPercentage() { return matchPercentage; }
        public String getSkills() { return skills; }
    }
} 