package com.system.flipkartminutes;

import com.system.flipkartminutes.entities.*;
import com.system.flipkartminutes.facade.OrderFacade;
import com.system.flipkartminutes.services.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@SpringBootApplication
public class FlipkartMinutesApplication {

    public static void main(String[] args) {
        // SpringApplication.run(FlipkartMinutesApplication.class, args);

      log.info("Starting FlipkartMinutesApplication");

      log.info("--------------------------------Pre-requisite Started -----------------------------------------");

        Product apple = new Product("Apple", "Fresh red apple", 10.0);
        Product banana = new Product("Banana", "Ripe yellow banana", 5.0);
        Product orange = new Product("Orange", "Juicy orange", 8.0);

        ProductRepository productRepository = ProductRepository.getInstance();
        productRepository.addProduct(apple);
        productRepository.addProduct(banana);
        productRepository.addProduct(orange);

        InventoryManager inventoryManager = InventoryManager.getInstance();
        inventoryManager.addInventory(apple.getProductId(), 1);
        inventoryManager.addInventory(banana.getProductId(), 1);
        inventoryManager.addInventory(orange.getProductId(), 1);

        Customer hitesh = new Customer("Hitesh", "8989438834", new Address("flat-1", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"), true);
        Customer hari = new Customer("Hari", "8989438835", new Address("flat-2", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"), true);
        CustomerManager customerManager = CustomerManager.getInstance();
        customerManager.addCustomer(hitesh);
        customerManager.addCustomer(hari);


        CartManager cartManager = CartManager.getInstance();
        Cart hiteshCart = cartManager.createCart(hitesh.getCustomerId());
        hiteshCart.addItem(apple);
        Cart hariCart = cartManager.createCart(hari.getCustomerId());
        hariCart.addItem(banana);

      log.info("--------------------------------Pre-requisite Ended -----------------------------------------");


      OrderFacade orderFacade = OrderFacade.getInstance();
      Order hiteshOrder = orderFacade.placeOrder(hiteshCart);   // should throw no delivery agent here found error
      orderFacade.showOrderDetails(hiteshOrder.getOrderId());

      DeliveryAgent agent = new DeliveryAgent("Anil", "89898989",  new Address("flat-1", "maathalli", "banfalire", "karnatake", "560909", "India", "neaer mulitplex"));
      DeliveryAgentManager deliveryAgentManager = DeliveryAgentManager.getInstance();
      deliveryAgentManager.addDeliveryAgent(agent);

      ///  again trying...
      Order hiteshOrder2 = orderFacade.placeOrder(hiteshCart);   // now able to creat order
      orderFacade.showOrderDetails(hiteshOrder2.getOrderId());

      Order hariOrder2 = orderFacade.placeOrder(hariCart);
      orderFacade.showOrderDetails(hariOrder2.getOrderId());


      // cancel order...
      orderFacade.cancelOrder(hiteshOrder2.getOrderId());
      

      orderFacade.showOrderDetails(hiteshOrder2.getOrderId());

      // orderFacade.placeOrder(hariCart);

      // view user history:
      orderFacade.viewHistory(hitesh.getCustomerId());

      // testing concurrency...
      Cart cart1 = cartManager.createCart(hitesh.getCustomerId());
      cart1.addItem(apple);
      Cart cart2 = cartManager.createCart(hari.getCustomerId());
      cart2.addItem(apple);


      log.info("--------------------Testing Concurrency and Locking ------------------------");
      // here inventory is 1 so either will get the apple other will fail...
      ExecutorService executorService = Executors.newFixedThreadPool(2);
      executorService.submit(() -> {orderFacade.placeOrder(cart1);});
      executorService.submit(() -> {orderFacade.placeOrder(cart2);});
      executorService.shutdown();

    }




}
