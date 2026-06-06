package com.system.flipkartminutes.services;

import com.system.flipkartminutes.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentService {

    // Singleton class (without springboot annorations like @Component or @Service)
    private PaymentService(){}
    private static PaymentService paymentService;
    public static PaymentService getInstance(){
        if(paymentService == null){
            synchronized (PaymentService.class){
                if(paymentService == null){
                    paymentService = new PaymentService();
                }
            }
        }
        return paymentService;
    }

    public PaymentStatus makePayment(){
        return PaymentStatus.SUCCESS;       // hard coding or mockin abhi ke liye
    }

}
