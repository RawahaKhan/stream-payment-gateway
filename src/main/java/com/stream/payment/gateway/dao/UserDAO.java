package com.stream.payment.gateway.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.stream.payment.gateway.entity.UserEntity;
import com.stream.payment.gateway.util.QueryConstants;

@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public Optional<UserEntity> findByUsername(String username) throws Exception{
        SqlRowSet rows = jdbcTemplate.queryForRowSet(QueryConstants.QRY_FIND_USER_BY_USERNAME, username);
        
        if (rows.next()) {
            UserEntity userEntity = mapRow(rows);
            return Optional.of(userEntity);
        } else {
            return Optional.empty(); 
        }
    }

    public UserEntity mapRow(SqlRowSet rs) {
        UserEntity user = new UserEntity();
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        return user;
    }
}

