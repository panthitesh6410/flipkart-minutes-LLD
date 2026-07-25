package com.system.flipkartminutes.strategy;

import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.services.DeliveryAgentManager;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
public class RatingBasedAgentStrategy implements IDeliveryAgentAssignStrategy {

    @Override
    public DeliveryAgent assignDeliveryAgent(String orderId) throws DeliveryAgentsFoundFound {
        DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();

        List<DeliveryAgent> availAgents = deliveryAgentManager.getAvailablrDeliveryAgents();

        if (availAgents.isEmpty()) {
            throw new DeliveryAgentsFoundFound("No available delivery agents found!");
        }

        Optional<DeliveryAgent> agent = availAgents.stream().max(Comparator.comparingDouble(DeliveryAgent::getRating));

        return agent.get();
    }
}