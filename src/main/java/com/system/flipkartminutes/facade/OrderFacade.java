package com.system.flipkartminutes.facade;

import com.system.flipkartminutes.entities.Cart;
import com.system.flipkartminutes.entities.DeliveryAgent;
import com.system.flipkartminutes.entities.Order;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.services.DeliveryAgentManager;
import com.system.flipkartminutes.services.OrderManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OrderFacade {

    // Singleton class:
    private OrderFacade(){}
    private static OrderFacade orderFacade;
    public static OrderFacade getInstance(){
        if(orderFacade == null){
            synchronized (OrderFacade.class){
                if(orderFacade == null){
                    orderFacade = new OrderFacade();
                }
            }
        }
        return orderFacade;
    }

    public Order placeOrder(Cart cart){
        OrderManager orderManager = OrderManager.getInstance();
        try{
            return orderManager.placeOrder(cart.getCartId());
        } catch(Exception e){
            log.error("Error in placeOrder", e);
        }
        return null;
    }

    public void cancelOrder(String orderId){
        OrderManager orderManager = OrderManager.getInstance();
        try{
            orderManager.cancelOrder(orderId);
        } catch(Exception e){
            log.error("Error in cancelOrder", e);
        }
    }

    public void viewHistory(String customerId){
        OrderManager orderManager = OrderManager.getInstance();
        List<Order> orders = orderManager.getOrderHistoryForCustomer(customerId);
        for (Order order : orders){
            log.info("Order " + order.getOrderId() + " Price - "+order.getTotalAmount() + ", Status - "+order.getOrderStatus());
        }
    }

    public void showOrderDetails(String orderId){
        OrderManager orderManager = OrderManager.getInstance();
        Order order = orderManager.getOrderById(orderId);
        log.info("Order Details for " + order.getOrderId() + ": " + order.getTotalAmount() +" ,Order Status - "+ order.getOrderStatus());
    }

    public void rateOrder(String orderId, double rating){
        OrderManager orderManager = OrderManager.getInstance();
        DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
        Order order = orderManager.getOrderById(orderId);
        if(order != null && !order.isRated()){
            DeliveryAgent deliveryAgent = order.getDeliveryAgent();
            try{
                deliveryAgentManager.rateDeliveryAgent(deliveryAgent.getAgentId(), rating);
                order.setRated(true);
                log.info("Succssfully Rated Devliery - {} with rating - {}", order.getOrderId(), rating);
            }  catch(DeliveryAgentsFoundFound e){
                log.error("Error in rateOrder: ", e);
            }
        } else{
            log.error("Either order is already rated or not found! - "+orderId);
        }
    }

    public void autoCancelExpiredOrders(long thresholdMs) {
        log.info("Running auto-cancel batch...");
        OrderManager.getInstance().autoCancelExpiredOrders(thresholdMs);
    }

    public void markDelivered(Order order){
        DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
        try{
            deliveryAgentManager.markDelivered(order.getDeliveryAgent().getAgentId(), order.getOrderId());
            log.info("Delivered Successfully Order {}", order.getOrderId());
        } catch (Exception e){
            log.error("Error in markDelivered", e);
        }
    }

    public void pickUpOrder(Order order){
        DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
        try{
            deliveryAgentManager.pickUpOrder(order.getDeliveryAgent().getAgentId(), order.getOrderId());
            log.info("Picked Up Order Successfully Order {} by {}", order.getOrderId(), order.getDeliveryAgent().getAgentName());
        } catch (Exception e){
            log.error("Error in markDelivered", e);
        }
    }

}
