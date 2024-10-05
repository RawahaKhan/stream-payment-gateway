package com.stream.payment.gateway.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class PaymentMethodDaoConfig {
	
    @Bean
    public DataSource dataSource(ConfigProperties configProperties) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(configProperties.getDriver());
        dataSource.setUrl(configProperties.getUrl());
        dataSource.setUsername(configProperties.getUsername());
        dataSource.setPassword(configProperties.getPassword());

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
