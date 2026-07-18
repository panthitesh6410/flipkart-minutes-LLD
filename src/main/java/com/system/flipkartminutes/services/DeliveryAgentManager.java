package com.system.flipkartminutes.services;

import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.entities.Order;
import com.system.flipkartminutes.enums.AgentStatus;
import com.system.flipkartminutes.enums.DevlieryAgentStrategyEnum;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.factory.DeliveryAgentFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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

    // CURRENTly hardocded the stratgeyy for delivery assign heere only  for now....
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

    public void rateDeliveryAgent(String agentId, double ratingToGive) throws DeliveryAgentsFoundFound {
        if (ratingToGive < 1.0 || ratingToGive > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (deliveryAgents == null) {
            deliveryAgents = new ConcurrentHashMap<>();
        }
        if (deliveryAgents.containsKey(agentId)) {
            DeliveryAgent deliveryAgent = deliveryAgents.get(agentId);
            int totalCount = deliveryAgent.getTotalRatingsCount();
            double totalSum = deliveryAgent.getTotalRatingSum();
            totalCount++;
            totalSum += ratingToGive;
            double avg = totalSum / totalCount;
            deliveryAgent.setTotalRatingsCount(totalCount);
            deliveryAgent.setTotalRatingSum(totalSum);
            deliveryAgent.setRating(avg);
        } else {
            throw new DeliveryAgentsFoundFound("No DeliveryAgent found for " + agentId);
        }
    }

    public void pickUpOrder(String agentId, String orderId) {
        // optionally validate the agent is actually assigned to this order
        Order order = OrderManager.getInstance().getOrderById(orderId);
        if (!order.getDeliveryAgent().getAgentId().equals(agentId)) {
            throw new RuntimeException("Agent not assigned to this order");
        }
        OrderManager.getInstance().pickUpOrder(orderId);
    }

    public void markDelivered(String agentId, String orderId) {
        Order order = OrderManager.getInstance().getOrderById(orderId);
        if (!order.getDeliveryAgent().getAgentId().equals(agentId)) {
            throw new RuntimeException("Agent not assigned to this order");
        }
        try {
            OrderManager.getInstance().markDelivered(orderId);
            log.info("Delivered order with orderId: " + orderId);
        } catch (DeliveryAgentsFoundFound e) {
            throw new RuntimeException(e);
        }
    }

}
