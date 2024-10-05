package com.stream.payment.gateway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stream.payment.gateway.entity.PaymentPlan;
import com.stream.payment.gateway.service.PaymentMethodFacade;

@RestController
@RequestMapping("/api/v1.0/configuration")
public class PaymentMethodController {
	
	@Autowired
    private PaymentMethodFacade paymentMethodFacade;
	
	@GetMapping("/payment-methods")
	public ResponseEntity<Map<String, Object>> getAllPaymentMethods(
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "country", required = false) String country) {

		return paymentMethodFacade.getFilteredPaymentMethods(name, id, country);
	}
	
	@PostMapping("/payment-methods")
	public ResponseEntity<Map<String, Object>> addPaymentMethods(@RequestBody String paymentMethods) {
		
		return paymentMethodFacade.addPaymentMethods(paymentMethods);
	}

	@PutMapping("/payment-methods")
	public ResponseEntity<Map<String, Object>> updatePaymentMethod(
			@RequestParam("id") String id, 
			@RequestBody String paymentMethodString) {
		
		return paymentMethodFacade.updatePaymentMethod(id, paymentMethodString);
	}
	
    @GetMapping("/payment-plans/duration")
    public ResponseEntity<Map<String, List<PaymentPlan>>> getGroupedPaymentPlansByDuration() {
       
    	return paymentMethodFacade.getGroupedPaymentPlansByDuration();
    }
}
