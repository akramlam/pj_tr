package com.example.client.ui.matches;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.client.R;
import java.util.ArrayList;
import java.util.List;

public class OpenProjectsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProjectsAdapter adapter;
    private List<MatchesFragment.Project> availableProjects;
    
    public static OpenProjectsFragment newInstance(List<MatchesFragment.Project> projects) {
        OpenProjectsFragment fragment = new OpenProjectsFragment();
        Bundle args = new Bundle();
        // Since we can't pass complex objects easily, we'll recreate the data
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_projects, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewProjects);
        swipeRefresh = view.findViewById(R.id.swipeRefreshProjects);
        
        setupRecyclerView();
        loadData();
        
        return view;
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProjectsAdapter(availableProjects, this::onProjectJoin);
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            swipeRefresh.setRefreshing(false);
        });
    }
    
    private void loadData() {
        // Create mock data for available projects
        availableProjects = new ArrayList<>();
        availableProjects.add(new MatchesFragment.Project("Smart Home IoT System", "Build an IoT system for home automation",
            "IoT, Arduino, Python, Mobile App", "Intermediate", "2-3 months", 3, 5));
        availableProjects.add(new MatchesFragment.Project("E-Learning Platform", "Create an interactive learning platform",
            "React, Node.js, MongoDB, Video Streaming", "Advanced", "4-6 months", 2, 6));
        availableProjects.add(new MatchesFragment.Project("Stock Price Predictor", "ML model for stock market analysis",
            "Machine Learning, Python, TensorFlow, Financial APIs", "Intermediate", "1-2 months", 4, 4));
        availableProjects.add(new MatchesFragment.Project("Social Media Analytics", "Analyze social media trends and sentiment",
            "NLP, Python, Twitter API, Data Visualization", "Advanced", "3-4 months", 1, 5));
        availableProjects.add(new MatchesFragment.Project("Recipe Recommendation App", "Mobile app for personalized recipes",
            "Android/iOS, Firebase, Machine Learning", "Beginner", "2-3 months", 5, 3));
        availableProjects.add(new MatchesFragment.Project("Blockchain Voting System", "Secure voting system using blockchain",
            "Blockchain, Solidity, Web3, React", "Advanced", "6+ months", 2, 8));
        
        if (adapter != null) {
            adapter.updateData(availableProjects);
        }
    }
    
    private void onProjectJoin(MatchesFragment.Project project) {
        // Handle project join logic
        // For now, just show a success message
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), 
                "Joined project: " + project.getTitle(), 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
} 