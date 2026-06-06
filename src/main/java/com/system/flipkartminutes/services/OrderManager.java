package com.system.flipkartminutes.services;


import com.system.flipkartminutes.entities.*;
import com.system.flipkartminutes.enums.OrderStatus;
import com.system.flipkartminutes.enums.PaymentStatus;
import com.system.flipkartminutes.exceptions.CartNotFoundException;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.exceptions.ProductNotAvailavbleException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class OrderManager {

    // Singleton class (without springboot annorations like @Component or @Service)
    private OrderManager(){}
    private static OrderManager orderManager;
    public static OrderManager getInstance(){
        if(orderManager == null){
            synchronized (OrderManager.class){
                if(orderManager == null){
                    orderManager = new OrderManager();
                }
            }
        }
        return orderManager;
    }


    private Map<String, Order> ordersByIdMap;
    private Map<String, List<String>> customerOrderMap;

    public Order placeOrder(String cartId) throws Exception {
        log.info("placing order for cart " + cartId);
        if (ordersByIdMap == null) {
            ordersByIdMap = new ConcurrentHashMap<>();
        }

        CartManager cartManager = CartManager.getInstance();
        DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
        CustomerManager customerManager = CustomerManager.getInstance();
        PaymentService paymentService = PaymentService.getInstance();
        LockManager  lockManager = LockManager.getInstance();
        InventoryManager inventoryManager = InventoryManager.getInstance();

        Cart cart = null;
        Customer customer = null;
        try{
            cart = cartManager.getCartById(cartId);
            customer = customerManager.getCustomerById(cart.getCustomerId());

        } catch (Exception e){
            log.error("Cart not found for cartId " + cartId);
            throw e;
        }

        List<Product> products = cart.getItems();
        for(Product product : products) {
            try{
                boolean locked = lockManager.acquireLock(product.getProductId());
                if(!locked){
                    log.error("Product Not Avvail - "+product.getProductId());
                    throw new ProductNotAvailavbleException("Cant Place Order, product Not avail - "+product.getProductId());
                } else {
                    log.info("Lock acquired for product " + product.getProductId());
                    int availQty = inventoryManager.getInventoryByProductId(product.getProductId());
                    if(availQty < 1){
                        log.error("Product Not Avvail in inventory - "+product.getProductId());
                        lockManager.releaseLock(product.getProductId());
                        throw new ProductNotAvailavbleException("Cant Place Order, product Not avail in inventory - "+product.getProductId());
                    }
                    inventoryManager.deductInventory(product.getProductId());
                }
            } finally {
                lockManager.releaseLock(product.getProductId());
                log.info("Lock released for product " + product.getProductId());
            }
        }

        // TODO - future me: create strategies fo Pricing also, based on like i have aded the membership in Customer classs , etc...
        double orderAmount = cart.getItems().stream().mapToDouble(item -> item.getPrice()).sum();
        Order order = Order
                .builder()
                .orderId(UUID.randomUUID().toString())
                .customer(customer)
                .orderPlaceTime(new Timestamp(System.currentTimeMillis()))
                .orderStatus(OrderStatus.PLACED)
                .totalAmount(orderAmount)
                .build();

        log.info("Order created for user - " + customer.getCustomerName() + " with order amount - " + orderAmount + " order status - " + order.getOrderStatus());

        populateOrderByIdMap(order);
        populateCustomerOrderMap(customer, order);

        PaymentStatus paymentStatus = paymentService.makePayment();
        order.setPaymentStatus(paymentStatus);
        log.info("Payment status for orderId - " + order.getOrderId() + " is " + paymentStatus);


        // assigning delivery-agent to order...
        DeliveryAgent agent = null;
        try {
            log.info("Assigning Delivery Partner...");
            agent =  deliveryAgentManager.assignDeliveryAgent(order);
            addAgentToOrder(order.getOrderId(), agent);
        } catch (DeliveryAgentsFoundFound e) {
            log.error("Cannot assign Delivery Partner - " + e.getMessage());
        }

        populateOrderByIdMap(order);
        populateCustomerOrderMap(customer, order);
        log.info("Order placed successfully for user " + customer.getCustomerName());

        return order;
    }

    public void populateOrderByIdMap(Order order) {
        if (ordersByIdMap == null) {
            ordersByIdMap = new HashMap<>();
        }
        ordersByIdMap.put(order.getOrderId(), order);
    }

    public void populateCustomerOrderMap(Customer customer, Order order) {
        if (customerOrderMap == null) {
            customerOrderMap = new HashMap<>();
        }
        if (!customerOrderMap.containsKey(customer.getCustomerId())) {
            customerOrderMap.put(customer.getCustomerId(), new java.util.ArrayList<>());
        }
        customerOrderMap.get(customer.getCustomerId()).add(order.getOrderId());
    }

    public void updateOrderStatus(String orderId, OrderStatus orderStatus) {
        ordersByIdMap.get(orderId).setOrderStatus(orderStatus);
        log.info("Order status updated for cart " + orderId + " to " + orderStatus);
    }

    public void addAgentToOrder(String orderId, DeliveryAgent agent) {
        log.info("Assigning agent " + agent.getAgentName() + " to order " + orderId);
        if(ordersByIdMap.get(orderId) == null){
            ordersByIdMap = new ConcurrentHashMap<>();
        }
        ordersByIdMap.get(orderId).setDeliveryAgent(agent);
        // log.info("Delivery agent - "+agent+" assigned successfully for order " + orderId);
    }

    public List<Order> getOrderHistoryForCustomer(String customerId) {
        CustomerManager customerManager = CustomerManager.getInstance();
        log.info("Fetching orders for customer " + customerId);
        List<String> orderIds = customerOrderMap.get(customerId);
        return orderIds.stream().map(orderId -> ordersByIdMap.get(orderId)).toList();
    }

    public void viewOrderHistoryForCustomer(String customerId) {
        List<Order> orders = getOrderHistoryForCustomer(customerId);
        for(Order order : orders) {
            log.info("OrderId: " + order.getOrderId() + " | Order Status: " + order.getOrderStatus() + " | Order Amount: " + order.getTotalAmount() +"\n");
        }
    }

    public Order getOrderById(String orderId) {
        if(ordersByIdMap.get(orderId) != null) {
            return ordersByIdMap.get(orderId);
        }
        return null;
    }

    public void cancelOrder(String orderId) throws Exception {
        Order order = getOrderById(orderId);
        if(order == null){
            log.error("Order not found for orderId " + orderId);
            throw new RuntimeException("Order not found for orderId " + orderId);
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        log.info("Order cancelled for orderId " + orderId);

        // releasing delivery agent if assigned
        if(order.getDeliveryAgent() != null){
            DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
            deliveryAgentManager.updateDeliveryAgentStatus(order.getDeliveryAgent().getAgentId(), com.system.flipkartminutes.enums.AgentStatus.AVAILABLE);
            log.info("Delivery agent released for orderId " + orderId);
        }

    }

}
