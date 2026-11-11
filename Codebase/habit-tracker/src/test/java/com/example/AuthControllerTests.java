package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTests {

    @Test
    void RegisterandAuthenticateUserNULL() { //test if user enters nothing for username and password: should pass as the assert is false
        UserAuth user = new UserAuth();
        assertFalse(user.SignUp(null, null));
        assertFalse(user.authorizeUser(null, null));
    } //test passed

    @Test 
    void UsernameINVALIDCHAR(){ //test for username with less than 7 charcters : should pass as assertion is false
        UserAuth user = new UserAuth();
        assertFalse(user.SignUp("Bushra", "password123"));
        assertFalse(user.authorizeUser("Bushra", "password123"));
    }//test passed

    @Test 
    void UsernameINVALIDTAKEN(){ //test for username that is already taken; should pass 
    UserAuth user = new UserAuth();
    assertTrue(user.SignUp("LOLLOLLOL", "password234"));
    assertFalse(user.SignUp("lollollol", "password234"));
    }//passed

    @Test 
    void passwordshort(){ //test for short password less than 8 characters should pass as assertion is false
    UserAuth user = new UserAuth();
    assertFalse(user.authorizeUser("Bushra12345", "234"));
    }//passed
    
    @Test 
    void signupthenauth(){ //test for signing up then authentiaction of username and password: should pass
    UserAuth user = new UserAuth();
    assertTrue(user.SignUp("Bushra234", "password1234"));
    assertTrue(user.authorizeUser("Bushra234", "password1234"));
    }//passed

}
