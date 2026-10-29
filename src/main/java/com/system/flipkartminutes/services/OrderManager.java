package com.system.flipkartminutes.services;


import com.system.flipkartminutes.entities.*;
import com.system.flipkartminutes.enums.AgentStatus;
import com.system.flipkartminutes.enums.OrderStatus;
import com.system.flipkartminutes.enums.PaymentStatus;
import com.system.flipkartminutes.exceptions.DeliveryAgentsFoundFound;
import com.system.flipkartminutes.exceptions.ProductNotAvailavbleException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.*;
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

    // watiingg queue:
    private Queue<Order> waitingOrderQueue = new LinkedList<>();


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
                boolean locked = lockManager.acquireProductLock(product.getProductId());
                if(!locked){
                    log.error("Product Not Avvail - "+product.getProductId());
                    throw new ProductNotAvailavbleException("Cant Place Order, product Not avail - "+product.getProductId());
                } else {
                    log.info("Lock acquired for product " + product.getProductId());
                    int availQty = inventoryManager.getInventoryByProductId(product.getProductId());
                    if(availQty < 1){
                        log.error("Product Not Avvail in inventory - "+product.getProductId());
                        lockManager.releaseProductLock(product.getProductId());
                        throw new ProductNotAvailavbleException("Cant Place Order, product Not avail in inventory - "+product.getProductId());
                    }
                    inventoryManager.deductInventory(product.getProductId());
                }
            } finally {
                lockManager.releaseProductLock(product.getProductId());
            }
        }

        PaymentStatus paymentStatus = paymentService.makePayment();
        log.info("Payment status is " + paymentStatus);


        // TODO - future me: create strategies fo Pricing also, based on like i have aded the membership in Customer classs , etc...
        double orderAmount = cart.getItems().stream().mapToDouble(item -> item.getPrice()).sum();
        Order order = Order
                .builder()
                .orderId(UUID.randomUUID().toString())
                .customer(customer)
                .orderPlaceTime(new Timestamp(System.currentTimeMillis()))
                .totalAmount(orderAmount)
                .paymentStatus(paymentStatus)
                .orderStatus(paymentStatus.equals(PaymentStatus.SUCCESS) ? OrderStatus.PLACED: OrderStatus.CANCELLED)
                .build();

        log.info("Order created for user - " + customer.getCustomerName() + " with order amount - " + orderAmount + " order status - " + order.getOrderStatus());

        populateOrderByIdMap(order);
        populateCustomerOrderMap(customer, order);

        if(order.getOrderStatus() != OrderStatus.PLACED){
            log.error("Order cannot be placed due to payment failure for user " + customer.getCustomerName());
            // refill inventory tht was deducted...
            releaseInventory(products);
            return order;
        }


        // assigning delivery-agent to order...
        DeliveryAgent agent = null;
        boolean isAgentLocked = false;
        try {
            log.info("Assigning Delivery Partner...");
            agent =  deliveryAgentManager.assignDeliveryAgent(order);
            isAgentLocked = lockManager.acquireAgentLock(agent.getAgentId());
            if(!isAgentLocked){
                log.error("Can assign Agent - {} to order - {} as Agent is already acquiered!", agent.getAgentName(), order.getOrderId());
                throw new DeliveryAgentsFoundFound("Agentt-"+ agent.getAgentName()+" is already acquiremd!");
            }
            deliveryAgentManager.updateDeliveryAgentStatus(agent.getAgentId(), AgentStatus.BUSY);
            addAgentToOrder(order.getOrderId(), agent);
        } catch (DeliveryAgentsFoundFound e) {
            log.error("Cannot assign Delivery Partner - " + e.getMessage());
            order.setOrderStatus(OrderStatus.CANCELLED);
            log.info("Order cancelled for user " + customer.getCustomerName() + " due to - " + e.getMessage());
            // refill inventory tht was deducted...
            releaseInventory(products);
            // move this order to a waiting queue abhi ke liiye....
            addOrderToWaitingQueue(order);
            // TODO - trigiger mech write code
            return order;
        } finally{
            if(agent != null && isAgentLocked)     lockManager.releaseAgentLock(agent.getAgentId());
        }

        populateOrderByIdMap(order);
        // populateCustomerOrderMap(customer, order);
        log.info("Order placed successfully for user " + customer.getCustomerName());

        return order;
    }

    //------------- captuore unfullfileled orders and triiger------------------------------
    private void addOrderToWaitingQueue(Order order) {
        waitingOrderQueue.add(order);
        log.info("Added Order {} to waiting Queue!!!", order.getOrderId());
    }

    public void placeWaitingOrders(){
        while(!waitingOrderQueue.isEmpty()){
            Order retryOrder = waitingOrderQueue.poll();
            log.info("Placing Order {} from Waiting List...", retryOrder.getOrderId());
            // placeOrder(order.getCartId());
        }
    }

    // ---------------------------------------------------------

    private void releaseInventory(List<Product> products) {
        InventoryManager inventoryManager = InventoryManager.getInstance();
        for(Product product : products){
            inventoryManager.addInventory(product.getProductId(), 1);
        }
        log.info("Inventory Loaded back...");
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

        // filter - as given in doc req - order cant be canlceed when picked....
        if (order.getOrderStatus() == OrderStatus.PICKED_UP ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel order already picked up or delivered");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        log.info("Order cancelled for orderId " + orderId);

        // releasing delivery agent if assigned
        if(order.getDeliveryAgent() != null){
            DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
            deliveryAgentManager.updateDeliveryAgentStatus(order.getDeliveryAgent().getAgentId(), com.system.flipkartminutes.enums.AgentStatus.IDLE);
            log.info("Delivery agent released for orderId " + orderId);
        }

        releaseInventory(order.getProducts());  // refill iteems to inentoryyy
    }

    // to be schdeulded by some CRON job if needed - just simulating that thinkg.. in BATCH kinda thinkg...
    // for all orders at once will mark...
    public void autoCancelExpiredOrders(long thresholdMs) {
        if (ordersByIdMap == null) return;
        long now = System.currentTimeMillis();
        ordersByIdMap.values().stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.PLACED).filter(o -> (now - o.getOrderPlaceTime().getTime()) > thresholdMs).forEach(o -> {
                    try {
                        cancelOrder(o.getOrderId());
                        log.info("Auto-cancelled expired order: {}", o.getOrderId());
                    } catch (Exception e) {
                        log.error("Error auto-cancelling order: {}", o.getOrderId(), e);
                    }
                });
    }


    // delivery ka status to uuppdated by agent ::
    // sirf delivery parntnetn can call these oneee::::

    public void pickUpOrder(String orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new RuntimeException(orderId+" Cannot pick up. Current status: " + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.PICKED_UP);
        log.info("Order {} picked up by agent {}", orderId, order.getDeliveryAgent().getAgentName());
    }


    public void markDelivered(String orderId) throws DeliveryAgentsFoundFound {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        if (order.getOrderStatus() != OrderStatus.PICKED_UP) {
            throw new RuntimeException("Cannot deliver. Current status: " + order.getOrderStatus());
        }
        order.setOrderStatus(OrderStatus.DELIVERED);
        // free the agent so they can take the next order
        DeliveryAgentManager.getInstance()
                .updateDeliveryAgentStatus(order.getDeliveryAgent().getAgentId(), AgentStatus.IDLE);
        log.info("Mark-Devlieded - Order {} delivered. Agent {} is now IDLE.", orderId, order.getDeliveryAgent().getAgentName());
    }




}
