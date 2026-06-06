package com.system.flipkartminutes.strategy;

import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;

public interface IDeliveryAgentAssignStrategy {

    DeliveryAgent assignDeliveryAgent(String orderId) throws DeliveryAgentsFoundFound;
}
