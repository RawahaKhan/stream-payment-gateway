package com.stream.payment.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiException extends RuntimeException {
   
	private static final long serialVersionUID = 1086350673028025049L;
	private String description;
    private String requestId;
    private int httpStatusCode;
}

