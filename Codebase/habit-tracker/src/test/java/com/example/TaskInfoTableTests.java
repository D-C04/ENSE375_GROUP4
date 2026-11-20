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

    @Test
    void UpdateTaskName_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskName(task.getTaskName(), "walk");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskName_OneExistingTask_NameIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskName(task.getTaskName(), "walk");

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskName = 'walk'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }

    @Test
    void UpdateTaskGoalAmount_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{

        int result = taskInfoTable.updateTaskGoalAmount("read", 5);

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskGoalAmount_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskGoalAmount(task.getTaskName(), 5);

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskGoalAmount_OneExistingTask_AmountIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskGoalAmount(task.getTaskName(), 5);

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskGoal = 2";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskGoal = 5";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }

    @Test
    void UpdateTimeOfDay_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{

        int result = taskInfoTable.updateTimeOfDay("read", "evening");

        assertEquals(0, result);

    }

    @Test
    void UpdateTimeOfDay_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTimeOfDay("read", "evening");

        assertEquals(1, result);

    }

    @Test
    void UpdateTimeOfDay_OneExistingTask_TimeOfDayIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTimeOfDay("read", "evening");

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskTimeOfDay = 'morning'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskTimeOfDay = 'evening'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }

    @Test
    void UpdateTaskUnit_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{

        int result = taskInfoTable.updateTaskUnit("read", "chapters");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskUnit_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskUnit("read", "chapters");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskUnit_OneExistingTask_UnitIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskUnit("read", "chapters");

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskUnit = 'pages'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskUnit = 'chapters'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }

    @Test
    void UpdateTaskStartDate_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{

        int result = taskInfoTable.updateTaskStartDate("read", "02-02-2022");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskStartDate_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskStartDate("read", "02-02-2022");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskStartDate_OneExistingTask_StartDateIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskStartDate("read", "02-02-2022");

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskStartDate = '01-09-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskStartDate = '02-02-2022'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }
    

}
