package com.example.client.ui.matches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {
    
    private List<MatchesFragment.Project> projects;
    private OnProjectJoinListener listener;
    
    public interface OnProjectJoinListener {
        void onProjectJoin(MatchesFragment.Project project);
    }
    
    public ProjectsAdapter(List<MatchesFragment.Project> projects, OnProjectJoinListener listener) {
        this.projects = projects != null ? projects : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        MatchesFragment.Project project = projects.get(position);
        holder.bind(project);
    }
    
    @Override
    public int getItemCount() {
        return projects != null ? projects.size() : 0;
    }
    
    public void updateData(List<MatchesFragment.Project> newProjects) {
        if (this.projects == null) {
            this.projects = new ArrayList<>();
        }
        this.projects.clear();
        if (newProjects != null) {
            this.projects.addAll(newProjects);
        }
        notifyDataSetChanged();
    }
    
    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView projectTitle;
        private TextView projectDescription;
        private TextView projectCategory;
        private TextView projectDifficulty;
        private TextView projectDuration;
        private TextView projectCreator;
        private TextView teamInfo;
        private MaterialButton joinButton;
        private MaterialCardView cardView;
        
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitle = itemView.findViewById(R.id.project_title);
            projectDescription = itemView.findViewById(R.id.project_description);
            projectCategory = itemView.findViewById(R.id.project_category);
            projectDifficulty = itemView.findViewById(R.id.project_difficulty);
            projectDuration = itemView.findViewById(R.id.project_duration);
            projectCreator = itemView.findViewById(R.id.project_creator);
            teamInfo = itemView.findViewById(R.id.team_info);
            joinButton = itemView.findViewById(R.id.join_project_button);
            cardView = itemView.findViewById(R.id.project_card);
        }
        
        public void bind(MatchesFragment.Project project) {
            projectTitle.setText(project.getTitle());
            projectDescription.setText(project.getDescription());
            projectCategory.setText(project.getCategory());
            projectDifficulty.setText(project.getDifficulty());
            projectDuration.setText(project.getDuration());
            projectCreator.setText("Created by " + project.getCreatedBy());
            
            int openSpots = project.getTeamSize() - project.getCurrentTeamSize();
            teamInfo.setText(String.format("Team: %d/%d (%d spots open)", 
                project.getCurrentTeamSize(), project.getTeamSize(), openSpots));
            
            joinButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectJoin(project);
                }
            });
        }
    }
} 