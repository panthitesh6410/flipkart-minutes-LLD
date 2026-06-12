package com.system.flipkartminutes.services;

import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.entities.Order;
import com.system.flipkartminutes.enums.AgentStatus;
import com.system.flipkartminutes.enums.DevlieryAgentStrategyEnum;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.factory.DeliveryAgentFactory;
import com.system.flipkartminutes.strategy.IDeliveryAgentAssignStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryAgentManager {

    // Singleton class (without springboot annorations like @Component or @Service)

    private DeliveryAgentManager(){}
    private static DeliveryAgentManager deliveryAgentManager;
    public static DeliveryAgentManager getInstance(){
        if(deliveryAgentManager == null){
            synchronized (DeliveryAgentManager.class){
                if(deliveryAgentManager == null){
                    deliveryAgentManager = new DeliveryAgentManager();
                }
            }
        }
        return deliveryAgentManager;
    }

    private Map<String, DeliveryAgent> deliveryAgents;

    public List<DeliveryAgent> getAvailablrDeliveryAgents(){
        if(deliveryAgents == null){
            deliveryAgents = new ConcurrentHashMap<>();
        }
        return deliveryAgents.values().stream().filter(agent->agent.getAgentStatus().equals(AgentStatus.IDLE)).toList();
    }

    public void addDeliveryAgent(DeliveryAgent deliveryAgent){
        if(deliveryAgents == null){
            deliveryAgents = new HashMap<>();
        }
        deliveryAgents.put(deliveryAgent.getAgentId(), deliveryAgent);
    }

    public synchronized DeliveryAgent assignDeliveryAgent(Order order) throws DeliveryAgentsFoundFound {
        com.system.flipkartminutes.strategy.IDeliveryAgentAssignStrategy factory = DeliveryAgentFactory.getDeliveryAgent(DevlieryAgentStrategyEnum.DEFAULT);
        DeliveryAgent agent = factory.assignDeliveryAgent(order.getOrderId());
        if(agent != null){
            agent.setAgentStatus(AgentStatus.BUSY);
            deliveryAgents.put(agent.getAgentId(), agent);
        }
        return agent;
    }

    public void updateDeliveryAgentStatus(String agentId, AgentStatus status) throws DeliveryAgentsFoundFound {
        if(deliveryAgents.containsKey(agentId)){
            DeliveryAgent deliveryAgent = deliveryAgents.get(agentId);
            deliveryAgent.setAgentStatus(status);
            deliveryAgents.put(agentId, deliveryAgent);
            return;
        }
        throw new DeliveryAgentsFoundFound("No DeliveryAgent found for "+ agentId);
    }

}
