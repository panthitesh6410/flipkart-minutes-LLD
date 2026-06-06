package com.system.flipkartminutes.services;


import com.system.flipkartminutes.entities.Cart;
import com.system.flipkartminutes.entities.Customer;
import com.system.flipkartminutes.entities.Product;
import com.system.flipkartminutes.exceptions.CartNotFoundException;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@ToString
public class CartManager {

    // Singleton class (without springboot annorations like @Component or @Service)
    private CartManager(){}
    private static CartManager cartManager;
    public static CartManager getInstance(){
        if(cartManager == null){
            synchronized (CartManager.class){
                if(cartManager == null){
                    cartManager = new CartManager();
                }
            }
        }
        return cartManager;
    }

    private Map<String, Cart> cartsById;
    private Map<String, List<String>> customerCartMap;

    public Cart getCartById(String cartId) throws CartNotFoundException {
        if(cartsById.containsKey(cartId)){
            return cartsById.get(cartId);
        }
        throw new CartNotFoundException("Cart not found");
    }

    public Cart createCart(String customerId){
        CustomerManager customerManager = CustomerManager.getInstance();
        Customer customer = null;
        try{
            customer = customerManager.getCustomerById(customerId);
        } catch (Exception e) {
            log.error("Error occurred while fetching customer", e);
        }
        Cart cart = new Cart(customerId);
        populateCartByIdMap(cart);
        populateCustomerCartMap(customer, cart);
        log.info("Cart created with id: "+cart.getCartId());
        return cart;
    }
    private void populateCartByIdMap(Cart cart){
        if(cartsById == null){
            cartsById = new HashMap<>();
        }
        cartsById.put(cart.getCartId(), cart);
    }

    private void populateCustomerCartMap(Customer customer, Cart cart){
        if(customerCartMap == null){
            customerCartMap = new HashMap<>();
        }
        if(!customerCartMap.containsKey(customer.getCustomerId())) {
            customerCartMap.put(customer.getCustomerId(), new java.util.ArrayList<>());
        }
        customerCartMap.get(customer.getCustomerId()).add(cart.getCartId());
    }

    public void addItemToCart(String cartId, Product product){
        InventoryManager inventoryManager = InventoryManager.getInstance();
        // check incentory first then allow only.
        try{
            int availQty = inventoryManager.getInventoryByProductId(product.getProductId());
            if(availQty <= 0){
                throw new RuntimeException("Product not available in inventory");
            }
            cartsById.get(cartId).getItems().add(product);
        } catch (Exception e) {
            log.error("Error occurred while checking inventory", e);
            throw new RuntimeException("Error occurred while adding item to cart", e);
        }

    }

    public void removeItemFromCart(String cartId, Product product){
        cartsById.get(cartId).getItems().remove(product);
    }

    public double calculateTotalPrice(String cartId){
        Cart cart = cartsById.get(cartId);
        return cart.getItems().stream().mapToDouble(Product::getPrice).sum();
    }

    public void getCartSummary(String cartId) throws CartNotFoundException {
        if(cartsById == null || !cartsById.containsKey(cartId)){
            throw new CartNotFoundException("Cart not found - "+ cartId);
        }
        Cart cart = cartsById.get(cartId);
        log.info("Cart Summary: \n"+cart.toString());
    }
}
