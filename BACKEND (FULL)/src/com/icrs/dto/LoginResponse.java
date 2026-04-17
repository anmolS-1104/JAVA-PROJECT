package com.icrs.dto;

public class LoginResponse {
    private int id;
    private String name;
    private String role;
    private String department;

    // Constructor for Customers (no access to the department variable)
    public LoginResponse(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // Constructor for Agents (with department variable)
    public LoginResponse(int id, String name, String role, String department) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
    }

    // Getters for private variables
    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
}