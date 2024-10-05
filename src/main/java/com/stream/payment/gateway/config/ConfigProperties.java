package com.stream.payment.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "com.stream.payment.gateway.config")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfigProperties {
	private String url;
	private String username;
	private String password;
	private String driver;
}

