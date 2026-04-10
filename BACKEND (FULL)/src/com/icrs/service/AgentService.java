package com.icrs.service;

import com.icrs.dao.AgentDAO;
import com.icrs.dao.AgentDAOImpl;
import com.icrs.model.Agent;

public class AgentService {

    private AgentDAO agentDAO = new AgentDAOImpl();

    public boolean register(Agent agent) {
        return agentDAO.registerAgent(agent);
    }

    public Agent login(String email, String password) {
        return agentDAO.loginAgent(email, password);
    }
}