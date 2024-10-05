package com.stream.payment.gateway.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.stream.payment.gateway.exception.ApiException;
import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.util.JwtUtil;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        	try {
	            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
	
	            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
	
	                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
	                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
	            }
        	}catch(DatabaseException e) {
        		fillResponse(response, e);
                return;
	        }
        }
        chain.doFilter(request, response);
    }
    
    private static void fillResponse(HttpServletResponse response, ApiException e) throws IOException {
        response.setStatus(e.getHttpStatusCode());
        response.setContentType("application/json");
        
        String jsonResponse = String.format(
            "{\"description\": \"%s\", \"httpStatusCode\": \"%d\", \"requestId\": \"%s\"}",
            e.getDescription(),
            e.getHttpStatusCode(),
            e.getRequestId()
        );

        response.getWriter().write(jsonResponse);
    	
    }
}

