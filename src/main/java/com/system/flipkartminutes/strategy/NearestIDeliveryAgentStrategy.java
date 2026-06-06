package com.system.flipkartminutes.strategy;

import com.system.flipkartminutes.entities.Customer;
import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.entities.Order;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.services.CustomerManager;
import com.system.flipkartminutes.services.DeliveryAgentManager;
import com.system.flipkartminutes.services.OrderManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NearestIDeliveryAgentStrategy implements IDeliveryAgentAssignStrategy {

        @Override
        public DeliveryAgent assignDeliveryAgent(String orderId) throws DeliveryAgentsFoundFound {

            DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
            CustomerManager customerManager = CustomerManager.getInstance();
            OrderManager orderManager = OrderManager.getInstance();


            try{
                Order order = orderManager.getOrderById(orderId);
                Customer customer = customerManager.getCustomerById(order.getCustomer().getCustomerId());

                List<DeliveryAgent> availableAgents = deliveryAgentManager.getAvailablrDeliveryAgents();
                if(!availableAgents.isEmpty()){
                    return availableAgents.stream().findFirst().get();
                } else{
                    throw new DeliveryAgentsFoundFound("No Delivery Agents Found near you location!");
                }

            } catch (Exception e){
                log.error("Error while assigning delivery agent for order id: "+orderId, e);
                throw new RuntimeException("Error while assigning delivery agent for order id: "+orderId);
            }

        }

}
