package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class UserModelTests {

    @Test
    void GetUserName_user1_true(){
        UserModel user1 = new UserModel("user1", "1234567890", "email@email.com");

        String username = user1.getUsername();
        
        assertEquals("user1", username);
    }

    @Test
    void GetUserName_user2_true(){
        UserModel user2 = new UserModel("user2", "1234567890", "email@email.com");

        String username = user2.getUsername();
        
        assertEquals("user2", username);
    }

    @Test
    void GetPasswordHash_1234567890_true(){
        UserModel user1 = new UserModel("user1", "1234567890", "email@email.com");

        String passwordHash = user1.getPasswordHash();

        assertTrue("1234567890" == passwordHash);
    }

    @Test
    void GetPasswordHash_password_true(){
        UserModel user1 = new UserModel("user1", "password", "email@email.com");

        String passwordHash = user1.getPasswordHash();

        assertTrue("password" == passwordHash);
    }

    @Test 
    void GetUserEmail_properEmail_true(){
        UserModel user1 = new UserModel("user1", "password", "email@email.com");

        String userEmail = user1.getUserEmail();

        assertTrue("email@email.com" == userEmail);
    }


}
