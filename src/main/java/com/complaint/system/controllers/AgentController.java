package com.complaint.system.controllers;

import com.complaint.system.model.Agent;
import com.complaint.system.service.AgentService;
import com.complaint.system.util.Session;

public class AgentController {

    private final AgentService agentService = new AgentService();

    /**
     * Agent authentication matching preset department credentials
     * and setting the active session state.
     */
    public Agent login(String email, String password) {
        if (email == null || password == null) {
            return null;
        }

        String cleanEmail = email.trim();
        String cleanPass = password.trim();
        Agent agent = null;

        // 1. Predefined Department Verification
        if (cleanEmail.equalsIgnoreCase("finance@agent.company.com") && cleanPass.equals("finance123")) {
            agent = createAgent(9001, "Finance Agent", cleanEmail, "Finance & Payroll");
        } else if (cleanEmail.equalsIgnoreCase("tech@agent.company.com") && cleanPass.equals("tech123")) {
            agent = createAgent(9002, "IT Support Agent", cleanEmail, "Technical Support");
        } else if (cleanEmail.equalsIgnoreCase("care@agent.company.com") && cleanPass.equals("care123")) {
            agent = createAgent(9003, "Customer Care Agent", cleanEmail, "Customer Care");
        } else {
            // 2. Database Fallback (if using an agents table)
            try {
                agent = agentService.login(cleanEmail, cleanPass);
            } catch (Exception e) {
                System.err.println("Agent DB service login error: " + e.getMessage());
            }
        }

        // 3. Establish Session
        if (agent != null) {
            Session.setUserId(agent.getId());
            Session.setUserName(agent.getFullName());
            Session.setUserEmail(agent.getEmail());
            Session.setUserDepartment(agent.getDepartment());
            Session.setUserRole("AGENT");
            System.out.println("Agent login successful! Welcome " + agent.getFullName() + " [" + agent.getDepartment() + "]");
        } else {
            System.out.println("Invalid Agent credentials.");
        }

        return agent;
    }

    private Agent createAgent(int id, String name, String email, String department) {
        Agent agent = new Agent();
        agent.setId(id);
        agent.setFullName(name);
        agent.setEmail(email);
        agent.setDepartment(department);
        return agent;
    }
}