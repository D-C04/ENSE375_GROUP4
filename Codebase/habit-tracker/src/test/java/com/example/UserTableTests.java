package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;

public class UserTableTests {

    private String dbFile = "habitTrackerTestTemp.db";
    private DatabaseSetup dbSetup;
    private UserTable userTable;

    @TempDir
    Path tempDir;

    // Creates a brand new temp database for each test
    @BeforeEach 
    void init() throws Exception {
        Files.deleteIfExists(Path.of(dbFile));
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
        dbSetup.createTables();
        userTable = new UserTable(dbSetup.openConnection());
    }

    @AfterEach 
    void clear() throws Exception {
        Files.deleteIfExists(Path.of(dbFile));
    }

    @Test
    void AddUser_NumOfRowsInTable_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);

        String query = "SELECT COUNT(*) FROM userTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    }

    @Test
    void AddUser_ValidateAddedCorrectUsernameSingleUser_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);

        String query = "SELECT COUNT(*) FROM userTable WHERE username = 'username1';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    } 

    @Test
    void AddUser_ValidateAddedCorrectPasswordHashSingleUser_one() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);

        String query = "SELECT COUNT(*) FROM userTable WHERE passwordHash = '1234567890';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    } 

    @Test
    void AddUser_ValidateAddedCorrectUserEmailSingleUser_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);

        String query = "SELECT COUNT(*) FROM userTable WHERE email = 'email@email.com';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    } 

    @Test
    void DeleteUser_NumOfRowsDeleted_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        int numRowsDeleted = userTable.deleteUser(user1.getUsername());

        assertEquals(1, numRowsDeleted);
        
    }

    @Test
    void UpdateUsername_IsNewUsernameInTable_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updateUsername(user1.getUsername(), "2usernameNew");

        String query = "SELECT COUNT(*) FROM userTable WHERE username = '2usernameNew';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
        
    }

    @Test
    void UpdateUsername_DidNewUsernameReplaceOld_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updateUsername(user1.getUsername(), "2usernameNew");

        String query = "SELECT COUNT(*) FROM userTable WHERE username = '2usernameNew' AND passwordHash = '1234567890' AND email = 'email@email.com';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 

        query = "SELECT COUNT(*) FROM userTable WHERE username = 'username1' AND passwordHash = '1234567890' AND email = 'email@email.com';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        } 
        
    }

    @Test
    void UpdateUsername_IsOldUsernameGone_Zero() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updateUsername(user1.getUsername(), "2usernameNew");

        String query = "SELECT COUNT(*) FROM userTable WHERE username = 'username1'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));;
        } 
        
    }

    @Test
    void UpdatePasswordHash_IsNewPasswordHashInTable_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updatePasswordHash(user1.getUsername(), "0987654321");

        String query = "SELECT COUNT(*) FROM userTable WHERE passwordHash = '0987654321';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
        
    }

    @Test
    void UpdatePasswordHash_DidNewPasswordHashReplaceOld_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updatePasswordHash(user1.getUsername(), "0987654321");

        String query = "SELECT COUNT(*) FROM userTable WHERE username = 'username1' AND passwordHash = '0987654321' AND email = 'email@email.com';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
        
    }

    @Test
    void UpdateEmail_IsNewEmailInTable_One() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");

        userTable.addUser(user1);
        userTable.updateEmail(user1.getUsername(), "username@gmail.com");

        String query = "SELECT COUNT(*) FROM userTable WHERE email = 'username@gmail.com';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
        
    }

    @Test
    void GetUserByUsername_DidGetAUser_NotNUll() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");
        userTable.addUser(user1);

        UserModel wantedUser = userTable.getUserByUsername(user1.getUsername());

        assertNotNull(wantedUser);
    }

    @Test
    void GetUserByUsername_DidGetCorrectUserName_true() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");
        userTable.addUser(user1);

        UserModel wantedUser1 = userTable.getUserByUsername(user1.getUsername());

        assertNotNull(wantedUser1);
        assertEquals("username1", wantedUser1.getUsername());
    }

    @Test
    void GetUserByUsername_GetMultipleUsers() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");
        UserModel user2 = new UserModel("2usernameNew", "0987654321", "username@gmail.com");

        userTable.addUser(user1);
        userTable.addUser(user2);

        UserModel wantedUser1 = userTable.getUserByUsername(user1.getUsername());
        UserModel wantedUser2 = userTable.getUserByUsername(user2.getUsername());

        assertEquals("username1", wantedUser1.getUsername());
        assertEquals("2usernameNew", wantedUser2.getUsername());
    }

    @Test
    void GetUserByUsername_UserNotExist_Null() throws SQLException{

        UserModel wantedUser1 = userTable.getUserByUsername("username1");

        assertNull(wantedUser1);
    }

    @Test
    void DoesUserExist_username1DoesExist_true() throws SQLException{

        UserModel user1 = new UserModel("username1", "1234567890", "email@email.com");
        userTable.addUser(user1);

        boolean doesUserExist = userTable.doesUserExist("username1");

        assertTrue(doesUserExist);
    }

    @Test
    void DoesUserExist_username1DoesNotExist_false() throws SQLException{

        boolean doesUserExist = userTable.doesUserExist("username1");

        assertFalse(doesUserExist);
    }





}
