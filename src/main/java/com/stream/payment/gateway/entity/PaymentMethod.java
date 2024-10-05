package com.stream.payment.gateway.entity;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod implements Serializable{

	private static final long serialVersionUID = -8198210109372070976L;
	private Integer id;
    private String name;
    private String displayName;
    private String paymentType;
    private String country;
    private List<PaymentPlan> paymentPlans;
}
