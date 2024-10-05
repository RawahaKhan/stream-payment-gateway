package com.stream.payment.gateway.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.microsoft.sqlserver.jdbc.SQLServerDataTable;
import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.stream.payment.gateway.entity.PaymentMethod;
import com.stream.payment.gateway.entity.PaymentPlan;
import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.exception.InvalidInputException;
import com.stream.payment.gateway.util.QueryConstants;
import com.stream.payment.gateway.util.RequestIdGenerator;

import io.jsonwebtoken.lang.Collections;

@Repository
public class PaymentMethodDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentMethodDao.class);

	public List<PaymentMethod> getPaymentMethods(String name, Integer id, String country) throws DatabaseException{
		List<PaymentMethod> paymentMethods = null;

		StringBuilder sql = new StringBuilder(QueryConstants.QRY_GET_PAYMENT_METHODS);

		List<Object> params = new ArrayList<>();

		if (StringUtils.isNotBlank(name)) {
			sql.append(QueryConstants.APPEND_PAYMENT_METHOD_NAME);
			params.add("%" + name + "%");
		} else if (id != 0) {
			sql.append(QueryConstants.APPEND_PAYMENT_PLAN_ID);
			params.add(id);  // For filtering by payment plan ID
		} else if (StringUtils.isNotBlank(country)) {
			sql.append(QueryConstants.APPEND_PAYMENT_METHOD_COUNTRY);
			params.add(country);  // For filtering by country
		}

		try {
			SqlRowSet rows = jdbcTemplate.queryForRowSet(sql.toString(), params.toArray());

			paymentMethods = fetchPaymentMethodList(rows);

		} catch (Exception e) {
			LOGGER.error("Error fetching payment methods: ", e);
			throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
		}

		return paymentMethods;
	}
	
	// Add payment methods and their associated payment plans
	public List<PaymentMethod> addPaymentMethods(List<PaymentMethod> paymentMethods) throws DatabaseException {
	    List<PaymentMethod> addedPaymentMethods = new ArrayList<>();

	    try {
	        for (PaymentMethod paymentMethod : paymentMethods) {
	            int generatedPaymentMethodId = insertPaymentMethod(paymentMethod);
	            
	            if (generatedPaymentMethodId > 0) {
	                paymentMethod.setId(generatedPaymentMethodId);

	                addPaymentPlans(paymentMethod.getPaymentPlans(), generatedPaymentMethodId);
	                addedPaymentMethods.add(paymentMethod);
	            } else {
	                LOGGER.error("Failed to generate ID for Payment Method: " + paymentMethod.getName());
	            }
	        }
	    } catch (Exception e) {
	        LOGGER.error("Error adding payment methods: ", e);
	        throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
	    }

	    return addedPaymentMethods;
	}
	

	@Transactional
	public PaymentMethod updatePaymentMethod(Integer id, PaymentMethod paymentMethod) throws DatabaseException, InvalidInputException {
	    try {
	        SQLServerDataTable paymentPlanDataTable = getPaymentPlanTableTypeSchema();

	        paymentMethod.getPaymentPlans().forEach(plan -> {
	            try {
	                paymentPlanDataTable.addRow(
	                    plan.getId(),
	                    plan.getNetAmount(),
	                    plan.getTaxAmount(),
	                    plan.getGrossAmount(),
	                    plan.getCurrency(),
	                    plan.getDuration()
	                );
	            } catch (SQLServerException e) {
	                LOGGER.error("Error while adding row to TVP: {}", e.getMessage());
	                throw new InvalidInputException(RequestIdGenerator.generateRequestId(), "Invalid payment plan details.");
	            }
	        });

	        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
	            QueryConstants.QRY_USP_UPDATE_PAYMENT_METHOD, id, paymentMethod.getName(),
	            paymentMethod.getDisplayName(), paymentMethod.getPaymentType(), paymentMethod.getCountry(),
	            paymentPlanDataTable
	        );
	        
	        Integer paymentMethodId = 0;
	        // Fetch the PaymentMethodID if available
	        if (rowSet.next()) {
	            paymentMethodId = rowSet.getInt("PaymentMethodID");
	        }

	        if (paymentMethodId == -1) {
	            throw new InvalidInputException(RequestIdGenerator.generateRequestId(), 
	                "Unable to update as the provided Payment method ID does not exist.");
	        } else {
	            return getUpdatedPaymentMethodWithPlans(id);
	        }

	    } catch (InvalidInputException e) {
	        throw e;
	    } catch (Exception e) {
	        LOGGER.error("Error while updating payment method with TVP: {}", e.getMessage());
	        throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
	    }
	}

	public Map<String, List<PaymentPlan>> getPaymentPlansGroupedByDuration() throws DataAccessException {
		Map<String, List<PaymentPlan>> groupedPlans = new HashMap<>();

		try {
			SqlRowSet row = jdbcTemplate.queryForRowSet(QueryConstants.QRY_GET_All_PAYMENT_PLANS);

			while (row.next()) {
				String duration = row.getString("Duration");

				Integer planId = row.getInt("PaymentPlanID");

				// Create PaymentPlan object
				BigDecimal grossAmountBD = row.getBigDecimal("GrossAmount");
				Double grossAmount = grossAmountBD != null ? grossAmountBD.doubleValue() : 0.00;

				BigDecimal netAmountBD = row.getBigDecimal("NetAmount");
				Double netAmount = netAmountBD != null ? netAmountBD.doubleValue() : 0.0;

				BigDecimal taxAmountBD = row.getBigDecimal("TaxAmount");
				Double taxAmount = taxAmountBD != null ? taxAmountBD.doubleValue() : 0.0;

				PaymentPlan plan = new PaymentPlan(
						planId,
						netAmount,
						taxAmount,
						grossAmount,
						row.getString("Currency"),
						duration
						);

				groupedPlans
				.computeIfAbsent(duration, k -> new ArrayList<>())
				.add(plan);
			}

		} catch (Exception e) {
			LOGGER.error("Error fetching payment methods: ", e);
			throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
		}

		return groupedPlans;
	}

	private PaymentMethod getUpdatedPaymentMethodWithPlans(Integer paymentMethodId) throws DatabaseException{
		try {
			PaymentMethod paymentMethod = null;
			
			StringBuilder sql = new StringBuilder(QueryConstants.QRY_GET_PAYMENT_METHODS);
			sql.append(QueryConstants.APPEND_PAYMENT_METHOD_ID);
			
			List<Object> params = new ArrayList<>();
			params.add(paymentMethodId);

			try {
				SqlRowSet rows = jdbcTemplate.queryForRowSet(sql.toString(), params.toArray());

				List<PaymentMethod> paymentMethodList = fetchPaymentMethodList(rows);

				if(!Collections.isEmpty(paymentMethodList)) {
					paymentMethod = paymentMethodList.get(0);
				}

			} catch (Exception e) {
				LOGGER.error("Error fetching payment methods: ", e);
				throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
			}

			return paymentMethod;
		} catch (Exception e) {
			LOGGER.error("Error retrieving updated payment method: ", e);
			throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
		}
	}

	private List<PaymentMethod> fetchPaymentMethodList(SqlRowSet rs) throws SQLException {
	    List<PaymentMethod> paymentMethods = new ArrayList<>();
	    Map<Integer, PaymentMethod> paymentMethodMap = new HashMap<>();

	    while (rs.next()) {
	        Integer paymentMethodId = rs.getInt("PaymentMethodID");

	        PaymentMethod paymentMethod = paymentMethodMap.computeIfAbsent(paymentMethodId, key -> {
                return new PaymentMethod(
                    paymentMethodId,
                    rs.getString("Name"),
                    rs.getString("DisplayName"),
                    rs.getString("PaymentType"),
                    rs.getString("Country"),
                    new ArrayList<>()
                );
	        });

	        if (rs.getObject("PaymentPlanID") != null) {
	            Integer planId = rs.getInt("PaymentPlanID");

	            BigDecimal grossAmountBD = rs.getBigDecimal("GrossAmount");
	            Double grossAmount = grossAmountBD != null ? grossAmountBD.doubleValue() : 0.00;

	            BigDecimal netAmountBD = rs.getBigDecimal("NetAmount");
	            Double netAmount = netAmountBD != null ? netAmountBD.doubleValue() : 0.0;

	            BigDecimal taxAmountBD = rs.getBigDecimal("TaxAmount");
	            Double taxAmount = taxAmountBD != null ? taxAmountBD.doubleValue() : 0.0;

	            
	            PaymentPlan plan = new PaymentPlan(
	                planId,
	                netAmount,
	                taxAmount,
	                grossAmount,
	                rs.getString("Currency"), 
	                rs.getString("Duration")         
	            );
	            paymentMethod.getPaymentPlans().add(plan);
	        }
	    }

	    paymentMethods.addAll(paymentMethodMap.values());
	    return paymentMethods;
	}

	private SQLServerDataTable getPaymentPlanTableTypeSchema() throws SQLServerException {
		SQLServerDataTable paymentPlanDataTable = new SQLServerDataTable();

		paymentPlanDataTable.addColumnMetadata("PaymentPlanID", java.sql.Types.INTEGER);  // Allow NULL for new plans
		paymentPlanDataTable.addColumnMetadata("NetAmount", java.sql.Types.DECIMAL);
		paymentPlanDataTable.addColumnMetadata("TaxAmount", java.sql.Types.DECIMAL);
		paymentPlanDataTable.addColumnMetadata("GrossAmount", java.sql.Types.DECIMAL);
		paymentPlanDataTable.addColumnMetadata("Currency", java.sql.Types.VARCHAR);
		paymentPlanDataTable.addColumnMetadata("Duration", java.sql.Types.VARCHAR);

		return paymentPlanDataTable;
	}
	

	private int insertPaymentMethod(PaymentMethod paymentMethod) {
	    KeyHolder keyHolder = new GeneratedKeyHolder();
	    jdbcTemplate.update(connection -> {
	        PreparedStatement ps = connection.prepareStatement(QueryConstants.QRY_ADD_PAYMENT_METHOD, Statement.RETURN_GENERATED_KEYS);
	        ps.setString(1, paymentMethod.getName());
	        ps.setString(2, paymentMethod.getDisplayName());
	        ps.setString(3, paymentMethod.getPaymentType());
	        ps.setString(4, paymentMethod.getCountry());
	        return ps;
	    }, keyHolder);

	    return (keyHolder.getKey() != null) ? keyHolder.getKey().intValue() : 0;
	}

	private void addPaymentPlans(List<PaymentPlan> paymentPlans, int paymentMethodId) {
	    for (PaymentPlan plan : paymentPlans) {
	        int generatedPaymentPlanId = insertPaymentPlan(plan, paymentMethodId);
	        
	        if (generatedPaymentPlanId > 0) {
	            plan.setId(generatedPaymentPlanId);
	        } else {
	        	plan.setId(-1);
	            LOGGER.error("Failed to generate ID for Payment Plan with method ID: " + paymentMethodId);
	        }
	    }
	}

	private int insertPaymentPlan(PaymentPlan plan, int paymentMethodId) {
	    KeyHolder keyHolder = new GeneratedKeyHolder();
	    jdbcTemplate.update(connection -> {
	        PreparedStatement ps = connection.prepareStatement(QueryConstants.QRY_ADD_PAYMENT_PLANS, Statement.RETURN_GENERATED_KEYS);
	        ps.setInt(1, paymentMethodId);
	        ps.setDouble(2, plan.getNetAmount() != null ? plan.getNetAmount() : 0.0);
	        ps.setDouble(3, plan.getTaxAmount() != null ? plan.getTaxAmount() : 0.0);
	        ps.setDouble(4, plan.getGrossAmount() != null ? plan.getGrossAmount() : 0.0);
	        ps.setString(5, plan.getCurrency());
	        ps.setString(6, plan.getDuration());
	        return ps;
	    }, keyHolder);

	    return (keyHolder.getKey() != null) ? keyHolder.getKey().intValue() : 0;
	}

}
