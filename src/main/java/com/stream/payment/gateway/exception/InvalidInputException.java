package com.stream.payment.gateway.exception;

public class InvalidInputException extends ApiException {
    
	private static final long serialVersionUID = 5898348959230257664L;

	public InvalidInputException(String requestId, String message) {
        super(message, requestId, 500);
    }
}

