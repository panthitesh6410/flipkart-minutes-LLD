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

    private Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(String productId){
        locks.putIfAbsent(productId, new ReentrantLock());
        ReentrantLock lock = locks.get(productId);

        if(lock.tryLock()){
            log.info("Lock acquired for product: " + productId);
            return true;
        } else {
            log.info("Failed to acquire lock for product: " + productId);
            return false;
        }
    }


    public void releaseLock(String productId){
        ReentrantLock lock = locks.get(productId);
        if(lock != null && lock.isHeldByCurrentThread()){
            lock.unlock();
            log.info("Released lock for product: " + productId);
        }
    }

}
