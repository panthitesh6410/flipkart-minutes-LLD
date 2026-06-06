package com.system.flipkartminutes.services;

import com.system.flipkartminutes.exceptions.ProductNotAvailavbleException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InventoryManager {

    // Singleton class (without springboot annorations like @Component or @Service)
    private InventoryManager(){}

    private static InventoryManager inventoryManager;

    public static InventoryManager getInstance(){
        if(inventoryManager == null){
            synchronized (InventoryManager.class){
                if(inventoryManager == null){
                    inventoryManager = new InventoryManager();
                }
            }
        }
        return inventoryManager;
    }

    private Map<String, Integer> inventoryByProductId;

    public void deductInventory(String productId){
        if(inventoryByProductId == null){
            inventoryByProductId = new ConcurrentHashMap<>();
        }
        if(inventoryByProductId.containsKey(productId)){
            inventoryByProductId.put(productId, inventoryByProductId.get(productId) - 1);
        } else{
            log.error("No Product Inventory Found for - "+ productId);
        }
    }

    public int getInventoryByProductId(String productId) throws ProductNotAvailavbleException{
        if(inventoryByProductId == null){
            inventoryByProductId = new ConcurrentHashMap<>();
        }
        if(!inventoryByProductId.containsKey(productId)){
            throw new ProductNotAvailavbleException("No Product Inventory Found for - "+ productId);
        }
        return inventoryByProductId.get(productId);
    }

    public void addInventory(String productId, int quantity){
        if(inventoryByProductId == null){
            inventoryByProductId = new ConcurrentHashMap<>();
        }
        if(!inventoryByProductId.containsKey(productId)){
            inventoryByProductId.put(productId, quantity);
        } else{
            inventoryByProductId.put(productId, inventoryByProductId.get(productId) + quantity);
        }
        log.info("Inventory Added! Inventory Balance: "+productId+" - "+inventoryByProductId.get(productId));
    }

}
