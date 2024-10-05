package com.stream.payment.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stream.payment.gateway.entity.PaymentMethod;
import com.stream.payment.gateway.entity.PaymentPlan;
import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.exception.InvalidInputException;
import com.stream.payment.gateway.util.RequestIdGenerator;

@Service
public class PaymentMethodFacade {
	@Autowired
    private PaymentMethodService paymentMethodService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMethodFacade.class);
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
    public ResponseEntity<Map<String, Object>> getFilteredPaymentMethods(String name, String id, String country) {
		try {
			int parsedId = 0;
			if(StringUtils.isNotBlank(id)) {
				parsedId= Integer.parseInt(id);
			}
			List<PaymentMethod> paymentMethods = paymentMethodService.getFilteredPaymentMethods(name, parsedId, country);

			Map<String, Object> response = new HashMap<>();
			response.put("paymentMethods", paymentMethods);

			return ResponseEntity.ok(response);
		} catch (NumberFormatException e) {
			throw new InvalidInputException(RequestIdGenerator.generateRequestId(), "Invalid ID format. Please provide a valid integer.");
		} catch (DatabaseException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("Error ->", e);
			throw e;
		}
	}

    public ResponseEntity<Map<String, Object>> addPaymentMethods(String paymentMethods) {
    	Map<String, Object> response = new HashMap<>();
    	try {
        	List<PaymentMethod> paymentMethodList = mapper.readValue(paymentMethods, new TypeReference<List<PaymentMethod>>() {});
             
            List<PaymentMethod> addedPaymentMethods = paymentMethodService.addPaymentMethods(paymentMethodList);

            response.put("paymentMethods", addedPaymentMethods);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
    	} catch (JsonProcessingException e) {
    		throw new InvalidInputException(RequestIdGenerator.generateRequestId(), e.getMessage());
    	} catch (DatabaseException e) {
            throw e; 
        } catch (Exception e) {
    		LOGGER.error("Error ->", e);
    		throw e;
    	}
    }
    
    public ResponseEntity<Map<String, Object>> updatePaymentMethod(String id, String paymentMethodString) {

        try {
        	int parsedId = Integer.parseInt(id);
        	
        	PaymentMethod paymentMethod = mapper.readValue(paymentMethodString, PaymentMethod.class);
        	
            PaymentMethod updatedPaymentMethod = paymentMethodService.updatePaymentMethod(parsedId, paymentMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("paymentMethod", updatedPaymentMethod);

            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
        	throw new InvalidInputException(RequestIdGenerator.generateRequestId(), "Invalid ID format. Please provide a valid integer.");
        } catch (JsonProcessingException e) {
        	throw new InvalidInputException(RequestIdGenerator.generateRequestId(), e.getMessage());
        } catch (DatabaseException | InvalidInputException e) {
        	throw e;
        } catch (Exception e) {
        	LOGGER.error("Error ->", e);
    		throw e;
    	}
    }
        
    public ResponseEntity<Map<String, List<PaymentPlan>>> getGroupedPaymentPlansByDuration() {
        try {
            // Call service to get grouped payment plans
            Map<String, List<PaymentPlan>> groupedPlans = paymentMethodService.getGroupedPaymentPlansByDuration();

            // Return the response with grouped plans
            return ResponseEntity.ok(groupedPlans);

        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
    		LOGGER.error("Error ->", e);
    		throw e;
    	}
    }
		
}
