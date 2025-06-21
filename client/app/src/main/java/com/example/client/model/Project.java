package com.example.client.model;

import java.util.UUID;

public class Project {
    private String id;
    private String title;
    private String description;

    public Project() {
        this.id = UUID.randomUUID().toString();
    }

    public Project(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    public Project(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return title != null ? title : "";
    }
} 