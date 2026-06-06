package com.system.flipkartminutes.facade;

import com.system.flipkartminutes.entities.Cart;
import com.system.flipkartminutes.entities.Order;
import com.system.flipkartminutes.services.CartManager;
import com.system.flipkartminutes.services.OrderManager;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OrderFacade {

    // Singleton class (without springboot annorations like @Component or @Service)
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

    public void placeOrder(Cart cart){
        OrderManager orderManager = OrderManager.getInstance();
        try{
            orderManager.placeOrder(cart.getCartId());
        }
        catch(Exception e){
            log.error("Error in placeOrder", e);
        }
    }

    public void cancelOrder(String orderId){
        OrderManager orderManager = OrderManager.getInstance();
        try{
            orderManager.cancelOrder(orderId);
        } catch(Exception e){
            log.error("Error in cancelOrder", e);
            try{
                orderManager.cancelOrder(orderId);
            } catch(Exception e1){
                log.error("Error in cancelOrder", e1);
            }
        }
    }

    public void viewHistory(String customerId){
        OrderManager orderManager = OrderManager.getInstance();
        List<Order> orders = orderManager.getOrderHistoryForCustomer(customerId);
        for (Order order : orders){
            log.info("Order " + order.getOrderId() + " Price - "+order.getTotalAmount());
        }
    }

}
