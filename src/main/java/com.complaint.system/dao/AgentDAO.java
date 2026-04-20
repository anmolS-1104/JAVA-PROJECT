package com.complaint.system.dao;

import com.complaint.system.model.Agent;

public interface AgentDAO {
    boolean registerAgent(Agent agent);
    Agent loginAgent(String email, String password);
}