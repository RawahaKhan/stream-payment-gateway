package com.stream.payment.gateway.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPlan implements Serializable{

	private static final long serialVersionUID = 8568135419171501662L;
	
	private Integer id;
    private Double netAmount;
    private Double taxAmount;
    private Double grossAmount;
    private String currency;
    private String duration;
	
}
