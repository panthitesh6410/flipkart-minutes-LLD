package com.system.flipkartminutes;

import com.system.flipkartminutes.entities.*;
import com.system.flipkartminutes.enums.OrderStatus;
import com.system.flipkartminutes.facade.OrderFacade;
import com.system.flipkartminutes.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.*;

@Slf4j
@SpringBootApplication
public class FlipkartMinutesApplication {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // SpringApplication.run(FlipkartMinutesApplication.class, args);

      log.info("Starting FlipkartMinutesApplication");

      log.info("Pre-requisite Started -");

      Product apple = new Product("Apple", "Fresh red apple", 10.0);
      Product banana = new Product("Banana", "Ripe yellow banana", 5.0);
      Product orange = new Product("Orange", "Juicy orange", 8.0);

      ProductRepository productRepository = ProductRepository.getInstance();
      productRepository.addProduct(apple);
      productRepository.addProduct(banana);
      productRepository.addProduct(orange);

      InventoryManager inventoryManager = InventoryManager.getInstance();
      inventoryManager.addInventory(apple.getProductId(), 4);
      inventoryManager.addInventory(banana.getProductId(), 4);
      inventoryManager.addInventory(orange.getProductId(), 4);

      Customer hitesh = new Customer("Hitesh", "8989438834", new Address("flat-1", "maathalli", "bangalire", "karnatake", "560909", "India", "neaer mulitplex"));
      Customer hari = new Customer("Hari", "8989438835", new Address("flat-2", "maathalli", "bangalire", "karnatake", "560909", "India", "neaer mulitplex"));
      CustomerManager customerManager = CustomerManager.getInstance();
      customerManager.addCustomer(hitesh);
      customerManager.addCustomer(hari);


      CartManager cartManager = CartManager.getInstance();
      Cart hiteshCart = cartManager.createCart(hitesh.getCustomerId());
      hiteshCart.addItem(apple);
      Cart hariCart = cartManager.createCart(hari.getCustomerId());
      hariCart.addItem(banana);

      log.info("Pre-requisite Ended -");


      OrderFacade orderFacade = OrderFacade.getInstance();
      Order hiteshOrder = orderFacade.placeOrder(hiteshCart);   // willl givev - no delivery agent here found error

      // adding 1 delivery agent to satrtt...
      DeliveryAgent agent = new DeliveryAgent("Anil", "89898989",  new Address("flat-1", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"));
      DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
      deliveryAgentManager.addDeliveryAgent(agent);



      ///  again trying...
      hiteshOrder = orderFacade.placeOrder(hiteshCart);   // now able to creat order
      orderFacade.showOrderDetails(hiteshOrder.getOrderId());
      orderFacade.pickUpOrder(hiteshOrder);
      orderFacade.showOrderDetails(hiteshOrder.getOrderId());
      orderFacade.markDelivered(hiteshOrder);
      orderFacade.showOrderDetails(hiteshOrder.getOrderId());


      // cancel order...
       orderFacade.cancelOrder(hiteshOrder.getOrderId());

      // RATE ORDDEER:
      orderFacade.rateOrder(hiteshOrder.getOrderId(), 4.0);


      // view user history:
      orderFacade.viewHistory(hitesh.getCustomerId());





      log.info("\n\n***********  Testing Concurrency and Locking");
      Cart cart1 = cartManager.createCart(hitesh.getCustomerId());
      cart1.addItem(apple);
      Cart cart2 = cartManager.createCart(hari.getCustomerId());
      cart2.addItem(apple);
      // here inventory is 1 left so either will get the apple other will fail.
      ExecutorService executorService = Executors.newFixedThreadPool(2);
      Future<Order> first = executorService.submit(() -> orderFacade.placeOrder(cart1));
      Future<Order> second = executorService.submit(() -> orderFacade.placeOrder(cart2));
      try {
        Order firstOrder = first.get();
        Order secondOrder = second.get();

        log.info("First order status: {}",
                firstOrder == null ? "FAILED" : firstOrder.getOrderStatus());
        log.info("Second order status: {}",
                secondOrder == null ? "FAILED" : secondOrder.getOrderStatus());
      } finally{
        executorService.shutdown();
      }



      // AUTOOOO CANCNELD  EXPIREED JOB :
    log.info("&&&&&&&&&&&&&&&&&&  testing autocancel jobn");
      DeliveryAgent da1 = new DeliveryAgent("Ramesh", "89898989", null);
      DeliveryAgentManager.getInstance().addDeliveryAgent(da1);
      Order order1 = orderFacade.placeOrder(cart1);
      orderFacade.showOrderDetails(order1.getOrderId()); // expect PLACED
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      orderFacade.autoCancelExpiredOrders(50);
      orderFacade.showOrderDetails(order1.getOrderId()); // expect CANCELLED

    }

}
