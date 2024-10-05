package com.stream.payment.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.util.JwtUtil;
import com.stream.payment.gateway.util.RequestIdGenerator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/api/v1.0/login")
    public String createToken(@RequestBody AuthRequest authRequest) throws Exception {
    	 final String jwt;
        try {
            authenticationManager.authenticate(
            		new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            		);

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            jwt = jwtUtil.generateToken(userDetails.getUsername());
        }catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(RequestIdGenerator.generateRequestId(), "Invalid username or password");
        }

        return jwt;
    }
}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
class AuthRequest {
    private String username;
    private String password;
}

