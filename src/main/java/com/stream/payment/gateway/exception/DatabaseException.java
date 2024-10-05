package com.stream.payment.gateway.exception;

public class DatabaseException extends ApiException {

	private static final long serialVersionUID = 4109574582954150297L;

	public DatabaseException(String requestId, String message) {
        super(message, requestId, 500);
    }
}

