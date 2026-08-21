package com.complaint.system.dto;

public class ComplaintDTO {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String category;
    private String department;
    private String priority;
    private String status;
    private String notes;
    private String createdAt;

    public ComplaintDTO() {}

    public ComplaintDTO(int id, String title, String description, String category, String department, String priority, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.department = department;
        this.priority = priority;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}