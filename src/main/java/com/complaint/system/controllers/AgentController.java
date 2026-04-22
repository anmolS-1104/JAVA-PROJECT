package com.complaint.system.controllers;

import com.complaint.system.model.Agent;
import com.complaint.system.service.AgentService;

public class AgentController {

    private AgentService agentService = new AgentService();

    public void register(String fullName, String email,
                         String password, String department) {
        Agent agent = new Agent();
        agent.setFullName(fullName);
        agent.setEmail(email);
        agent.setPassword(password);
        agent.setDepartment(department);

        boolean success = agentService.register(agent);
        if (success) {
            System.out.println("Agent registered successfully!");
        } else {
            System.out.println("Agent registration failed.");
        }
    }

    public Agent login(String email, String password) {
        Agent agent = agentService.login(email, password);
        if (agent != null) {
            System.out.println("Agent login successful! Welcome " + agent.getFullName());
        } else {
            System.out.println("Invalid email or password.");
        }
        return agent;
    }
}