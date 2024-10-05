package com.stream.payment.gateway.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.stream.payment.gateway.dao.UserDAO;
import com.stream.payment.gateway.entity.UserEntity;
import com.stream.payment.gateway.exception.DatabaseException;
import com.stream.payment.gateway.util.RequestIdGenerator;

@Service
public class MyUserDetailsService implements UserDetailsService {

	
	@Autowired
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws DataAccessException {
    	try {
	        Optional<UserEntity> userEntityOpt = userDAO.findByUsername(username);
	        
	        if (userEntityOpt.isPresent()) {
	            UserEntity userEntity = userEntityOpt.get();
	            return new User(userEntity.getUsername(), new BCryptPasswordEncoder().encode(userEntity.getPassword()), new ArrayList<>());
	        } else {
	        	throw new DatabaseException(RequestIdGenerator.generateRequestId(), "User not found with username: " + username);
	        }
    	}catch (DataAccessException e) {
    		throw new DatabaseException(RequestIdGenerator.generateRequestId(), 
    				"Unable to access user details due to a database error. Please check your connection settings.");
        } catch (Exception e) {
        	throw new DatabaseException(RequestIdGenerator.generateRequestId(), e.getMessage());
        }
    }
}

