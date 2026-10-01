package com.complaint.system.dto;

import java.io.Serializable;

public class ComplaintDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int userId;
    private String title;
    private String description;
    private String userEmail;
    private String category;
    private String department;
    private String priority;
    private String status;
    private String notes;

    // Enterprise client verification fields
    private String clientCompanyName;
    private String clientOrgCode;

    public ComplaintDTO() {
    }

    public ComplaintDTO(int id, int userId, String title, String description, String userEmail,
                        String category, String department, String priority, String status,
                        String notes, String clientCompanyName, String clientOrgCode) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.userEmail = userEmail;
        this.category = category;
        this.department = department;
        this.priority = priority;
        this.status = status;
        this.notes = notes;
        this.clientCompanyName = clientCompanyName;
        this.clientOrgCode = clientOrgCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getClientCompanyName() {
        return clientCompanyName;
    }

    public void setClientCompanyName(String clientCompanyName) {
        this.clientCompanyName = clientCompanyName;
    }

    public String getClientOrgCode() {
        return clientOrgCode;
    }

    public void setClientOrgCode(String clientOrgCode) {
        this.clientOrgCode = clientOrgCode;
    }
}