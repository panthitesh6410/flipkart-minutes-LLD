package com.system.flipkartminutes.services;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LockManager {
    // Singleton class (without springboot annorations like @Component or @Service)
    private LockManager(){}
    private static LockManager lockManager;
    public static LockManager getInstance(){
        if(lockManager == null){
            synchronized (LockManager.class){
                if(lockManager == null){
                    lockManager = new LockManager();
                }
            }
        }
        return lockManager;
    }

    private Map<String, ReentrantLock> productLocks = new ConcurrentHashMap<>();

    private Map<String, ReentrantLock> deliveryAgentLocks = new ConcurrentHashMap<>();

    public boolean acquireProductLock(String productId){
        productLocks.putIfAbsent(productId, new ReentrantLock());
        ReentrantLock lock = productLocks.get(productId);

        if(lock.tryLock()){
            log.info("Lock acquired for product: " + productId);
            return true;
        } else {
            log.info("Failed to acquire lock for product: " + productId);
            return false;
        }
    }


    public void releaseProductLock(String productId){
        ReentrantLock lock = productLocks.get(productId);
        if(lock != null && lock.isHeldByCurrentThread()){
            lock.unlock();
            log.info("Released lock for product: " + productId);
        }
    }

    // -----------------copy -paste karo for delivery agent lockkkks

    public boolean acquireAgentLock(String agentId){
        deliveryAgentLocks.putIfAbsent(agentId, new ReentrantLock());
        ReentrantLock lock = deliveryAgentLocks.get(agentId);

        if(lock.tryLock()){
            log.info("Lock acquired for Delivery Agent: " + agentId);
            return true;
        } else {
            log.info("Failed to acquire lock for DveliryAgent: " + agentId);
            return false;
        }
    }


    public void releaseAgentLock(String agentId){
        ReentrantLock lock = deliveryAgentLocks.get(agentId);
        if(lock != null && lock.isHeldByCurrentThread()){
            lock.unlock();
            log.info("Released lock for Devlivery Agentt: " + agentId);
        }
    }



}
