package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.internal.matchers.Null;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class DatabaseSetupTests {

    private String dbFile = "habitTrackerTestTemp.db";
    private DatabaseSetup dbSetup;

    @TempDir
    Path tempDir; 

    // Creates a brand new temp database for each test
    @BeforeEach 
    void init() throws Exception {
        Files.deleteIfExists(Path.of(dbFile));
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
    }

    @AfterEach 
    void clear() throws Exception {
        Files.deleteIfExists(Path.of(dbFile));
    }

    @Test
    void OpenConnection_isNotNull_true() throws SQLException{

        try (Connection conn = dbSetup.openConnection()) {
            assertNotNull(conn);
        } 

    }

    @Test
    void OpenConnection_isNotClosed_true() throws SQLException{

        try (Connection conn = dbSetup.openConnection()) {
            assertTrue(!conn.isClosed());
        } 

    }

    @Test
    void CreateTables_CreateUserTable_True() throws SQLException{

        dbSetup.createTables(); 
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'userTable';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    }

    @Test
    void CreateTables_CreateNoteTable_True() throws SQLException{

        dbSetup.createTables(); 
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'noteTable';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    }

    @Test
    void CreateTables_CreateGenTaskTable_True() throws SQLException{

        dbSetup.createTables(); 
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'genTaskTable';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    }

    @Test
    void CreateTables_CreateTaskLogTable_True() throws SQLException{

        dbSetup.createTables(); 
        String query = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'taskLogTable';";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        } 
    }


}
