package com.example.client.ui.profile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.client.R;
import com.example.client.adapter.ProjectAdapter;
import com.example.client.model.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectManagerDialog extends Dialog implements ProjectAdapter.OnProjectActionListener {

    public interface OnProjectsChangedListener {
        void onProjectsChanged(List<Project> projects);
    }

    private EditText etProjectTitle;
    private EditText etProjectDescription;
    private Button btnAddProject;
    private Button btnSaveProjects;
    private Button btnCancelProjects;
    private ImageButton btnCloseProjectDialog;
    private RecyclerView recyclerProjects;
    private TextView tvNoProjects;

    private ProjectAdapter projectAdapter;
    private List<Project> projects;
    private OnProjectsChangedListener listener;
    private Project editingProject; // For edit mode

    public ProjectManagerDialog(@NonNull Context context, List<Project> initialProjects, OnProjectsChangedListener listener) {
        super(context);
        this.projects = new ArrayList<>(initialProjects);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_project_manager);

        // Set dialog properties
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        setupRecyclerView();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        etProjectTitle = findViewById(R.id.etProjectTitle);
        etProjectDescription = findViewById(R.id.etProjectDescription);
        btnAddProject = findViewById(R.id.btnAddProject);
        btnSaveProjects = findViewById(R.id.btnSaveProjects);
        btnCancelProjects = findViewById(R.id.btnCancelProjects);
        btnCloseProjectDialog = findViewById(R.id.btnCloseProjectDialog);
        recyclerProjects = findViewById(R.id.recyclerProjects);
        tvNoProjects = findViewById(R.id.tvNoProjects);
    }

    private void setupRecyclerView() {
        projectAdapter = new ProjectAdapter(this);
        recyclerProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerProjects.setAdapter(projectAdapter);
        projectAdapter.setProjects(projects);
    }

    private void setupListeners() {
        btnAddProject.setOnClickListener(v -> addOrUpdateProject());
        
        btnSaveProjects.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectsChanged(projectAdapter.getAllProjects());
            }
            dismiss();
        });
        
        btnCancelProjects.setOnClickListener(v -> dismiss());
        btnCloseProjectDialog.setOnClickListener(v -> dismiss());
    }

    private void addOrUpdateProject() {
        String title = etProjectTitle.getText().toString().trim();
        String description = etProjectDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a project title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingProject != null) {
            // Update existing project
            editingProject.setTitle(title);
            editingProject.setDescription(description);
            projectAdapter.updateProject(editingProject);
            btnAddProject.setText("Add Project");
            editingProject = null;
        } else {
            // Add new project
            Project newProject = new Project(title, description);
            projectAdapter.addProject(newProject);
        }

        // Clear form
        etProjectTitle.setText("");
        etProjectDescription.setText("");
        updateUI();
    }

    private void updateUI() {
        boolean hasProjects = projectAdapter.getItemCount() > 0;
        tvNoProjects.setVisibility(hasProjects ? View.GONE : View.VISIBLE);
        recyclerProjects.setVisibility(hasProjects ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEditProject(Project project) {
        // Fill form with project data for editing
        editingProject = project;
        etProjectTitle.setText(project.getTitle());
        etProjectDescription.setText(project.getDescription());
        btnAddProject.setText("Update Project");
        
        // Scroll to top to show the form
        if (getWindow() != null && getWindow().getDecorView() instanceof ViewGroup) {
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            decorView.scrollTo(0, 0);
        }
    }

    @Override
    public void onDeleteProject(Project project) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete \"" + project.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    projectAdapter.removeProject(project);
                    updateUI();
                    
                    // If we were editing this project, clear the form
                    if (editingProject != null && editingProject.equals(project)) {
                        etProjectTitle.setText("");
                        etProjectDescription.setText("");
                        btnAddProject.setText("Add Project");
                        editingProject = null;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Helper method to convert string list to Project list (for backward compatibility)
    public static List<Project> fromStringSet(java.util.Set<String> projectStrings) {
        List<Project> projects = new ArrayList<>();
        if (projectStrings != null) {
            for (String projectString : projectStrings) {
                if (projectString != null && !projectString.trim().isEmpty()) {
                    // Try to parse if it contains a description separator
                    if (projectString.contains(" - ")) {
                        String[] parts = projectString.split(" - ", 2);
                        projects.add(new Project(parts[0].trim(), parts[1].trim()));
                    } else {
                        projects.add(new Project(projectString.trim(), ""));
                    }
                }
            }
        }
        return projects;
    }

    // Helper method to convert Project list to string set (for API compatibility)
    public static java.util.Set<String> toStringSet(List<Project> projects) {
        java.util.Set<String> projectStrings = new java.util.HashSet<>();
        if (projects != null) {
            for (Project project : projects) {
                String projectString = project.getTitle();
                if (project.getDescription() != null && !project.getDescription().trim().isEmpty()) {
                    projectString += " - " + project.getDescription();
                }
                projectStrings.add(projectString);
            }
        }
        return projectStrings;
    }
} 