package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TaskInfoTableTests {
    private String dbFile = "habitTrackerTestTemp.db";
    private DatabaseSetup dbSetup;
    private TaskInfoTable taskInfoTable;

    @TempDir
    Path tempDir;

    // Creates a brand new temp database for each test
    @BeforeEach 
    void init() throws Exception {
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
        dbSetup.createTables();
        taskInfoTable = new TaskInfoTable(dbSetup);
    }

    @Test
    void AddTask_firstTask_OneRowInTable() throws SQLException{

        String query = "SELECT COUNT(*) FROM genTaskTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
         
        }
    }
}
