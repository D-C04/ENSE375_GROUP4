package com.example;


/**
 * UserModel - Holds a user's information so it can be passed to and from the database classes.
 * Holds: username, passwordHash, and userEmail.
 * Immutable: values are set in the constructor and can only be read with getters.
 */
public class UserModel {

    private final String username;
    private final String passwordHash; // Make sure the password is hashed. Do not put in plain text passwords.
    private final String userEmail;

    /**
     * Constructor for the user model. 
     * Only holds the data. Does not do any validation, hashing, or check if values are unique.
     * 
     * @param username the user's username
     * @param passwordHash the user's passwordHash
     * @param userEmail the user's email
     */
    public UserModel(String username, String passwordHash, String userEmail){
        this.username = username;
        this.passwordHash = passwordHash;
        this.userEmail = userEmail;
    }

    public String getUsername(){
        return username;
    }

    public String getPasswordHash(){
        return passwordHash;
    }

    public String getUserEmail(){
        return userEmail;
    }

}
