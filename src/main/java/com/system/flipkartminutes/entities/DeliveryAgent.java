package com.system.flipkartminutes.entities;

import com.system.flipkartminutes.enums.AgentStatus;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class DeliveryAgent {
    private String agentId;
    private String agentName;
    private String phoneNumber;
    private AgentStatus agentStatus;
    private Address location;
    public DeliveryAgent(String agentName, String phoneNumber, Address location) {
        this.agentId = UUID.randomUUID().toString();
        this.agentName = agentName;
        this.phoneNumber = phoneNumber;
        this.agentStatus = AgentStatus.IDLE;
        this.location = location;
    }
}
