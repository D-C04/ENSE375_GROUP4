package com.example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class UserAuth {
private final Map<String, String> users = new ConcurrentHashMap<>();
private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    //conditions for username and passwords:
    //username:
        //cannot be null
        //cannot be used from before by another user
        //has to be string: includes numbers, letters and special characters
        //username length has to be greater than 7 characters
    //password:
        //has to be og length 8 and more
        //includes all characters
        //cannot be null
    
    public boolean SignUp (String username, String password){
        if (username ==null || username.isBlank() ||username.length()<7) 
            return false;
        if (password ==null || password.isBlank() || password.length() <8)
            return false;
        return users.putIfAbsent(username.trim().toLowerCase(), encoder.encode(password)) ==null; //turn password into a BCrypt hash, then add username and pass to map if they are not already there
    }

    public boolean authorizeUser(String username, String password){
        if(username==null ||password==null) 
            return false;
        String hash = users.get(username.trim().toLowerCase());//look for stored hash for that username
        return hash != null && encoder.matches(password, hash); //check whether user exits or not
    }
}
