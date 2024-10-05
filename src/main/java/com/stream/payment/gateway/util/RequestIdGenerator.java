package com.stream.payment.gateway.util;

import java.util.UUID;

public class RequestIdGenerator {
	
    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}

