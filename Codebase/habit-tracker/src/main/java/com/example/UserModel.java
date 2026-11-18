package com.example;

public class UserModel {

    private String username;
    private String passwordHash;
    private String userEmail;

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

}
