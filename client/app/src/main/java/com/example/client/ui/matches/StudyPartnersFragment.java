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

public class StudyPartnersFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private StudyPartnersAdapter adapter;
    private List<MatchesFragment.PotentialMatch> potentialMatches;
    
    public static StudyPartnersFragment newInstance(List<MatchesFragment.PotentialMatch> matches) {
        StudyPartnersFragment fragment = new StudyPartnersFragment();
        Bundle args = new Bundle();
        // Since we can't pass complex objects easily, we'll recreate the data
        fragment.setArguments(args);
        return fragment;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_partners, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewStudyPartners);
        swipeRefresh = view.findViewById(R.id.swipeRefreshStudyPartners);
        
        setupRecyclerView();
        loadData();
        
        return view;
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StudyPartnersAdapter(potentialMatches);
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            swipeRefresh.setRefreshing(false);
        });
    }
    
    private void loadData() {
        // Create mock data for study partners
        potentialMatches = new ArrayList<>();
        potentialMatches.add(new MatchesFragment.PotentialMatch("Alice Chen", "Computer Science", 
            "Looking for ML study partner", "Machine Learning, Python, TensorFlow", 85));
        potentialMatches.add(new MatchesFragment.PotentialMatch("David Rodriguez", "Data Science", 
            "Working on NLP project", "Natural Language Processing, BERT, PyTorch", 92));
        potentialMatches.add(new MatchesFragment.PotentialMatch("Sarah Kim", "Software Engineering", 
            "Mobile app development", "Android, Kotlin, Firebase", 78));
        potentialMatches.add(new MatchesFragment.PotentialMatch("Mohammed Hassan", "AI Research", 
            "Deep learning enthusiast", "Computer Vision, OpenCV, YOLO", 88));
        potentialMatches.add(new MatchesFragment.PotentialMatch("Emma Johnson", "Web Development", 
            "Full-stack developer", "React, Node.js, MongoDB", 81));
        
        if (adapter != null) {
            adapter.updateData(potentialMatches);
        }
    }
} 