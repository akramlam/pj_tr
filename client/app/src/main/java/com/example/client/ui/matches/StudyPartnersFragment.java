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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        potentialMatches = new ArrayList<>(); // Initialize the list before creating adapter
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
        
        Set<String> skills1 = new HashSet<>();
        skills1.add("Machine Learning");
        skills1.add("Python");
        potentialMatches.add(new MatchesFragment.PotentialMatch("Alice Chen", "Computer Science", skills1, 85));
        
        Set<String> skills2 = new HashSet<>();
        skills2.add("Natural Language Processing");
        skills2.add("PyTorch");
        potentialMatches.add(new MatchesFragment.PotentialMatch("David Rodriguez", "Data Science", skills2, 92));
        
        Set<String> skills3 = new HashSet<>();
        skills3.add("Android");
        skills3.add("Kotlin");
        potentialMatches.add(new MatchesFragment.PotentialMatch("Sarah Kim", "Software Engineering", skills3, 78));
        
        Set<String> skills4 = new HashSet<>();
        skills4.add("Computer Vision");
        skills4.add("OpenCV");
        potentialMatches.add(new MatchesFragment.PotentialMatch("Mohammed Hassan", "AI Research", skills4, 88));
        
        Set<String> skills5 = new HashSet<>();
        skills5.add("React");
        skills5.add("Node.js");
        potentialMatches.add(new MatchesFragment.PotentialMatch("Emma Johnson", "Web Development", skills5, 81));
        
        if (adapter != null) {
            adapter.updateData(potentialMatches);
        }
    }
} 