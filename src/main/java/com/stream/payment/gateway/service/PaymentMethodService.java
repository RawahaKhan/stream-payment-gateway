package com.stream.payment.gateway.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stream.payment.gateway.dao.PaymentMethodDao;
import com.stream.payment.gateway.entity.PaymentMethod;
import com.stream.payment.gateway.entity.PaymentPlan;
import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.exception.InvalidInputException;

@Service
public class PaymentMethodService {

    @Autowired
    private PaymentMethodDao paymentMethodDao;

    public List<PaymentMethod> getFilteredPaymentMethods(String name, Integer id, String country) {
        try {
            return paymentMethodDao.getPaymentMethods(name, id, country);
        } catch (DatabaseException e) {
            throw e; 
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching payment methods", e);
        }
    }
    
    public List<PaymentMethod> addPaymentMethods(List<PaymentMethod> paymentMethods) {
        try {
            return paymentMethodDao.addPaymentMethods(paymentMethods);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching payment methods", e);
        }
    }

    // Update payment method by ID
    public PaymentMethod updatePaymentMethod(Integer id, PaymentMethod paymentMethod) {
        try {
            return paymentMethodDao.updatePaymentMethod(id, paymentMethod);
        } catch (DatabaseException | InvalidInputException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching payment methods", e);
        }
    }
    
    public Map<String, List<PaymentPlan>> getGroupedPaymentPlansByDuration() {
        try {
            return paymentMethodDao.getPaymentPlansGroupedByDuration();
        } catch (DatabaseException e) {
            throw e; 
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while fetching payment methods", e);
        }
    }
    
}

