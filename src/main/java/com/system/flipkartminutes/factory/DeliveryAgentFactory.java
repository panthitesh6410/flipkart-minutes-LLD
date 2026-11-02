package com.system.flipkartminutes.factory;

import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.enums.DevlieryAgentStrategyEnum;
import com.system.flipkartminutes.strategy.DefaultDeliveryAgentStrategy;
import com.system.flipkartminutes.strategy.IDeliveryAgentAssignStrategy;
import com.system.flipkartminutes.strategy.NearestIDeliveryAgentStrategy;
import com.system.flipkartminutes.strategy.RatingBasedAgentStrategy;

public class DeliveryAgentFactory {
    public static IDeliveryAgentAssignStrategy getDeliveryAgent(DevlieryAgentStrategyEnum strategyEnum){
        switch(strategyEnum){
            case NEAREST:
                return new NearestIDeliveryAgentStrategy();
            case DEFAULT:
                return new DefaultDeliveryAgentStrategy();
            case RATING_BASED:
                return new RatingBasedAgentStrategy();
            default:
                throw new IllegalArgumentException("Invalid Devlivery Agent strategy type: " + strategyEnum);
        }
    }
}
