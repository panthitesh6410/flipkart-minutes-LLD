package com.system.flipkartminutes.services;

import com.system.flipkartminutes.entities.Product;
import com.system.flipkartminutes.exceptions.ProductNotAvailavbleException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProductRepository {

    // Singleton class (without springboot annorations like @Component or @Service)
    private ProductRepository(){}

    private static ProductRepository productRepository;

    public static ProductRepository getInstance(){
        if(productRepository == null){
            synchronized (CustomerManager.class){
                if(productRepository == null){
                    productRepository = new ProductRepository();
                }
            }
        }
        return productRepository;
    }

    private Map<String, Product> products;

    public void addProduct(Product product){
         if(products == null){
             products = new ConcurrentHashMap<>();
         }
         products.put(product.getProductId(), product);
         log.info("Product {} has been added", product.getProductId());
    }

    public Product getProduct(String productId) throws ProductNotAvailavbleException {
        if(products == null){
            products = new ConcurrentHashMap<>();
        }
        if(!products.containsKey(productId)){
            throw new ProductNotAvailavbleException("Product with id "+productId+") is not available");
        }
        return products.get(productId);
    }

}
