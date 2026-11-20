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
    private UserTable userTable;

    @TempDir
    Path tempDir;

    // Creates a brand new temp database for each test
    @BeforeEach 
    void init() throws Exception {
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
        dbSetup.createTables();
        taskInfoTable = new TaskInfoTable(dbSetup);
        userTable = new UserTable(dbSetup);
    }

    @Test
    void AddTask_FirstTask_OneRowInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "morning", "pages", "jan-09-2026", "dec-21-2026");
        taskInfoTable.addTask(task);

        String query = "SELECT COUNT(*) FROM genTaskTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTask_FirstTask_CorrectInfoInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        String query = "SELECT COUNT(*) FROM genTaskTable WHERE userId = 1 AND taskName = 'read' AND "
            + "taskGoal = 2 AND taskUnit = 'pages' AND taskTimeOfDay = 'morning' AND taskStartDate = '01-09-2026' "
            + "AND taskEndDate = '12-21-2026'";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void DeleteTask_FirstTask_ReturnsOne() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.deleteTask(task.getTaskName());

        assertEquals(1, result);
    }

    @Test
    void DeleteTask_TaskDoesNotExist_ReturnsZero() throws SQLException{

        int result = taskInfoTable.deleteTask("read");

        assertEquals(0, result);
    }

    @Test
    void DeleteTask_FirstTask_ZeroRowsInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        taskInfoTable.deleteTask(task.getTaskName());

        String query = "SELECT COUNT(*) FROM genTaskTable";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void UpdateTaskName_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{

        int result = taskInfoTable.updateTaskName("read", "walk");

        assertEquals(0, result);

    }
    

}
