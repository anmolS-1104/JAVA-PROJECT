package com.complaint.system.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Fallback getters/setters for alternate JSON property naming
    public String getFullName() { return name; }
    public void setFullName(String fullName) { this.name = fullName; }

    public String getFull_name() { return name; }
    public void setFull_name(String full_name) { this.name = full_name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}