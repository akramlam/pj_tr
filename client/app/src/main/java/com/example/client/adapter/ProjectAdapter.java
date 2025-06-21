package com.example.client.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects;
    private OnProjectActionListener listener;

    public interface OnProjectActionListener {
        void onEditProject(Project project);
        void onDeleteProject(Project project);
    }

    public ProjectAdapter(OnProjectActionListener listener) {
        this.projects = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public void setProjects(List<Project> projects) {
        this.projects = new ArrayList<>(projects);
        notifyDataSetChanged();
    }

    public void addProject(Project project) {
        projects.add(project);
        notifyItemInserted(projects.size() - 1);
    }

    public void updateProject(Project updatedProject) {
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId().equals(updatedProject.getId())) {
                projects.set(i, updatedProject);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeProject(Project project) {
        int position = projects.indexOf(project);
        if (position != -1) {
            projects.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(projects);
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProjectTitle;
        private TextView tvProjectDescription;
        private ImageButton btnEditProject;
        private ImageButton btnDeleteProject;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProjectTitle = itemView.findViewById(R.id.tvProjectTitle);
            tvProjectDescription = itemView.findViewById(R.id.tvProjectDescription);
            btnEditProject = itemView.findViewById(R.id.btnEditProject);
            btnDeleteProject = itemView.findViewById(R.id.btnDeleteProject);
        }

        public void bind(Project project) {
            tvProjectTitle.setText(project.getTitle());
            
            // Show/hide description based on content
            if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
                tvProjectDescription.setText(project.getDescription());
                tvProjectDescription.setVisibility(View.VISIBLE);
            } else {
                tvProjectDescription.setVisibility(View.GONE);
            }

            // Set click listeners
            btnEditProject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditProject(project);
                }
            });

            btnDeleteProject.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProject(project);
                }
            });
        }
    }
} 