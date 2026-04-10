package com.icrs.dao;

import com.icrs.model.Agent;

public interface AgentDAO {
    boolean registerAgent(Agent agent);
    Agent loginAgent(String email, String password);
}