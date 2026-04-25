package com.icrs.model;

public class Complaint {
    private int id;
    private String description;
    private String priority;
    private String department;
    private int userId; // links complaint to a user

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}