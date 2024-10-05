package com.stream.payment.gateway.util;

public class QueryConstants {
	
	public static final String QRY_GET_PAYMENT_METHODS = 
	        "SELECT pm.PaymentMethodID, pm.Name, pm.DisplayName, pm.PaymentType, pm.Country, pp.PaymentPlanID, pp.NetAmount, pp.TaxAmount, pp.GrossAmount, pp.Currency, pp.Duration " +
	        "FROM payment.PaymentMethods pm " +
	        "LEFT JOIN payment.PaymentPlans pp ON pm.PaymentMethodID = pp.PaymentMethodID " +
	        "WHERE 1=1 ";
	
	public static final String QRY_GET_All_PAYMENT_PLANS = "SELECT PaymentPlanID, NetAmount, TaxAmount, GrossAmount, Currency, Duration "
			+ "FROM payment.PaymentPlans where Duration IS NOT NULL";
	
	public static final String QRY_ADD_PAYMENT_METHOD = "INSERT INTO payment.PaymentMethods (Name, DisplayName, PaymentType, Country) VALUES (?, ?, ?, ?)";
	
	public static final String QRY_UPDATE_PAYMENT_METHOD = "UPDATE payment.PaymentMethods SET Name = ?, DisplayName = ?, PaymentType = ?, Country = ? WHERE PaymentMethodID = ?";
	
	
	public static final String QRY_ADD_PAYMENT_PLANS = "INSERT INTO payment.PaymentPlans (PaymentMethodID, NetAmount, TaxAmount, GrossAmount, Currency, Duration) VALUES (?, ?, ?, ?, ?, ?)";
	
	public static final String QRY_UPDATE_PAYMENT_PLANS = "UPDATE payment.PaymentPlans SET NetAmount = ?, TaxAmount = ?, GrossAmount = ?, Currency = ?, Duration = ? WHERE PaymentPlanID = ?";

	public static final String QRY_USP_UPDATE_PAYMENT_METHOD = "EXEC [payment].[USP_UPDATE_PAYMENT_METHOD] ?, ?, ?, ?, ?, ?";

	
	public static final String APPEND_PAYMENT_METHOD_NAME = " AND pm.Name LIKE ? ";
	public static final String APPEND_PAYMENT_PLAN_ID = " AND pp.PaymentPlanID = ? ";
	public static final String APPEND_PAYMENT_METHOD_COUNTRY = " AND pm.Country = ? ";
	public static final String APPEND_PAYMENT_METHOD_ID = " AND pm.PaymentMethodID = ? ";
	
	
	public static final String QRY_FIND_USER_BY_USERNAME = "SELECT username, password FROM security.users WHERE username = ?";
}
