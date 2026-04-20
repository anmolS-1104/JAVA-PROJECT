package com.complaint.system.service;

import com.complaint.system.dao.AgentDAO;
import com.complaint.system.dao.AgentDAOImpl;
import com.complaint.system.model.Agent;

public class AgentService {

    private AgentDAO agentDAO = new AgentDAOImpl();

    public boolean register(Agent agent) {
        return agentDAO.registerAgent(agent);
    }

    public Agent login(String email, String password) {
        return agentDAO.loginAgent(email, password);
    }
}