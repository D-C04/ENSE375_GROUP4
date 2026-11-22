package com.example;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteException;

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

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "jan-09-2026", "dec-21-2026");
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
    void AddTask_DuplicateTaskNameForUser_ThrowsSQLException() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel task2 = new TaskModel(1, "read", 9, "chapters", "all day", "02-09-2026", "11-21-2026");
        
        assertThrows(SQLException.class, () -> taskInfoTable.addTask(task2));
        
    }

    @Test
    void AddTask_DuplicateTaskNameForDifferentUsers_DoesNotThrowSQLException() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel task2 = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        
        assertDoesNotThrow(() -> taskInfoTable.addTask(task2));
        
    }

    @Test
    void AddTask_DuplicateOtherTaskInfo_DoesNotThrowSQLException() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel task2 = new TaskModel(1, "write", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        
        assertDoesNotThrow(() -> taskInfoTable.addTask(task2));
        
    }

    @Test
    void AddTask_NameWithSqlCharacters_StoredExactly() throws SQLException {

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int userId = userTable.getUserId(user.getUsername());

        String attemptedInjection = "x'); DROP TABLE genTaskTable;--";
        TaskModel task = new TaskModel(userId, attemptedInjection, 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel found = taskInfoTable.getSingleTask(userId, attemptedInjection);
        assertNotNull(found);
        assertEquals(attemptedInjection, found.getTaskName());
    }

    @Test
    void AddTask_userNotExist_ThrowsSQLiteException() throws SQLException {

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
    
        assertThrows(SQLiteException.class, () -> taskInfoTable.addTask(task));
    }



    @Test
    void DeleteTask_FirstTask_ReturnsOne() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.deleteTask(userTable.getUserId(user.getUsername()), task.getTaskName());

        assertEquals(1, result);
    }

    @Test
    void DeleteTask_TaskDoesNotExist_ReturnsZero() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.deleteTask(userTable.getUserId(user.getUsername()), "read");

        assertEquals(0, result);
    }

    @Test
    void DeleteTask_FirstTask_ZeroRowsInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        taskInfoTable.deleteTask(userTable.getUserId(user.getUsername()), task.getTaskName());

        String query = "SELECT COUNT(*) FROM genTaskTable";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void DeleteTask_DiffUsersHaveSameTaskName_OneRowInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        TaskModel tasks = new TaskModel(userTable.getUserId(user2.getUsername()), "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(tasks);
        taskInfoTable.deleteTask(userTable.getUserId(user2.getUsername()), task.getTaskName());

        String query = "SELECT COUNT(*) FROM genTaskTable";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void DeleteTask_DiffUsersHaveSameTaskName_TaskForCorrectUserIsDeleted() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        TaskModel tasks = new TaskModel(userTable.getUserId(user2.getUsername()), "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(tasks);

        String query = "SELECT COUNT(*) FROM genTaskTable WHERE userId = 2 AND taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        taskInfoTable.deleteTask(userTable.getUserId(user2.getUsername()), task.getTaskName());

        query = "SELECT COUNT(*) FROM genTaskTable WHERE userId = 1 AND taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable WHERE userId = 2 AND taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    void DeleteTask_InjectionString_DeletesNothing() throws SQLException {
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        
        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        String attemptedInjection = "' OR '1'='1";
        int numRowsDeleted = taskInfoTable.deleteTask(userTable.getUserId(user.getUsername()), attemptedInjection);

        assertEquals(0, numRowsDeleted);
        assertNotNull(taskInfoTable.getSingleTask(userTable.getUserId(user.getUsername()), "read"));
    }



    @Test
    void UpdateTaskName_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTaskName(userTable.getUserId(user.getUsername()), "read", "walk");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskName_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskName(userTable.getUserId(user.getUsername()), task.getTaskName(), "walk");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskName_OneExistingTask_NameIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskName(userTable.getUserId(user.getUsername()), task.getTaskName(), "walk");

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
    void UpdateTaskName_DiffUsersHaveTaskWithSameName_NameIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTaskName(userTable.getUserId(user.getUsername()), task.getTaskName(), "walk");

        query = "SELECT COUNT(*) FROM genTaskTable Where taskName = 'read'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
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
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTaskGoalAmount(userTable.getUserId(user.getUsername()), "read", 5);

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskGoalAmount_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskGoalAmount(userTable.getUserId(user.getUsername()), task.getTaskName(), 5);

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskGoalAmount_OneExistingTask_AmountIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskGoalAmount(userTable.getUserId(user.getUsername()), task.getTaskName(), 5);

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
    void UpdateTaskGoalAmount_DiffUsersHaveTaskWithSameName_AmountIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskGoal = 2";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTaskGoalAmount(userTable.getUserId(user.getUsername()), task.getTaskName(), 5);

        query = "SELECT COUNT(*) FROM genTaskTable Where taskGoal = 2";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
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
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTimeOfDay(userTable.getUserId(user.getUsername()), "read", "evening");

        assertEquals(0, result);

    }

    @Test
    void UpdateTimeOfDay_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTimeOfDay(userTable.getUserId(user.getUsername()), "read", "evening");

        assertEquals(1, result);

    }

    @Test
    void UpdateTimeOfDay_OneExistingTask_TimeOfDayIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTimeOfDay(userTable.getUserId(user.getUsername()), "read", "evening");

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
    void UpdateTimeOfDay_DiffUsersHaveTaskWithSameName_TimetIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskTimeOfDay = 'morning'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTimeOfDay(userTable.getUserId(user.getUsername()), "read", "evening");

        query = "SELECT COUNT(*) FROM genTaskTable Where taskTimeOfDay = 'morning'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
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
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTaskUnit(userTable.getUserId(user.getUsername()), "read", "chapters");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskUnit_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskUnit(userTable.getUserId(user.getUsername()), "read", "chapters");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskUnit_OneExistingTask_UnitIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskUnit(userTable.getUserId(user.getUsername()), "read", "chapters");

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
    void UpdateTaskUnit_DiffUsersHaveTaskWithSameName_UnitIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskUnit = 'pages'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTaskUnit(userTable.getUserId(user.getUsername()), "read", "chapters");

        query = "SELECT COUNT(*) FROM genTaskTable Where taskUnit = 'pages'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
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
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTaskStartDate(userTable.getUserId(user.getUsername()), "read", "02-02-2022");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskStartDate_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskStartDate(userTable.getUserId(user.getUsername()), "read", "02-02-2022");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskStartDate_OneExistingTask_StartDateIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskStartDate(userTable.getUserId(user.getUsername()), "read", "02-02-2022");

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

    @Test
    void UpdateTaskStartDate_DiffUsersHaveTaskWithSameName_StartDateIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskStartDate = '01-09-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTaskStartDate(userTable.getUserId(user.getUsername()), "read", "02-02-2022");

        query = "SELECT COUNT(*) FROM genTaskTable Where taskStartDate = '01-09-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskStartDate = '02-02-2022'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }



    @Test
    void UpdateTaskEndDate_NumOfChangedRowsTaskDoesNotExist_ZeroIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int result = taskInfoTable.updateTaskEndDate(userTable.getUserId(user.getUsername()), "read", "02-02-2028");

        assertEquals(0, result);

    }

    @Test
    void UpdateTaskEndDate_NumOfChangedRowsTaskDoesExist_OneIsReturned() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int result = taskInfoTable.updateTaskEndDate(userTable.getUserId(user.getUsername()), "read", "02-02-2028");

        assertEquals(1, result);

    }

    @Test
    void UpdateTaskEndDate_OneExistingTask_EndDateIsChangedInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        taskInfoTable.updateTaskEndDate(userTable.getUserId(user.getUsername()), "read", "02-02-2028");

        String query = "SELECT COUNT(*) FROM genTaskTable Where taskEndDate = '12-21-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskEndDate = '02-02-2028'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }
    
    @Test
    void UpdateTaskEndDate_DiffUsersHaveTaskWithSameName_EndDateIsChangedInDbForCorrectUser() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        
        String query = "SELECT COUNT(*) FROM genTaskTable Where taskEndDate = '12-21-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }

        taskInfoTable.updateTaskEndDate(userTable.getUserId(user.getUsername()), "read", "02-02-2028");

        query = "SELECT COUNT(*) FROM genTaskTable Where taskEndDate = '12-21-2026'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM genTaskTable Where taskEndDate = '02-02-2028'";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

    }


    
    @Test
    void GetTaskByUserId_userDoesNotExist_EmptyListIsReturned() throws SQLException{

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByUserId(1);

        assertTrue(userTaskArray.isEmpty());

    }

    @Test
    void GetTaskByUserId_userHasNoTasks_EmptyListIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByUserId(userTable.getUserId(user.getUsername()));

        assertTrue(userTaskArray.isEmpty());

    }

    @Test
    void GetTaskByUserId_userHasOneTask_ListOfLengthOneIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByUserId(userTable.getUserId(user.getUsername()));

        assertEquals(1, userTaskArray.size());;

    }

    @Test
    void GetTaskByUserId_userHasFiveTask_ListOfLengthFiveIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "1", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "2", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "3", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "4", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "5", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByUserId(userTable.getUserId(user.getUsername()));

        assertEquals(5, userTaskArray.size());;

    }

    @Test
    void GetTaskByUserId_userHasTwoTasksMultUsersHaveTasks_ListOfLengthTwoIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "1", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "0987654321", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(userTable.getUserId(user2.getUsername()), "1", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(userTable.getUserId(user2.getUsername()), "2", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        List<TaskModel> userTaskArray = taskInfoTable.getTasksByUserId(userTable.getUserId(user2.getUsername()));

        assertEquals(2, userTaskArray.size());;

    }



    @Test
    void GetTaskByTimeOfDay_userDoesNotExist_EmptyListIsReturned() throws SQLException{

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByTimeOfDay(1, "morning");

        assertTrue(userTaskArray.isEmpty());

    }

    @Test
    void GetTaskByTimeOfDay_userHasNoTasks_EmptyListIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByTimeOfDay(userTable.getUserId(user.getUsername()), "morning");

        assertTrue(userTaskArray.isEmpty());

    }

    @Test
    void GetTaskByTimeOfDay_userHasOneMorningTask_ListOfLengthOneIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByTimeOfDay(userTable.getUserId(user.getUsername()), "morning");

        assertEquals(1, userTaskArray.size());;

    }

    @Test
    void GetTaskByTimeOfDay_correctNumOfEveningTasksOtherTimesExist_ListOfLength3IsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(1, "1", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "2", 2, "pages", "evening", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "3", 2, "pages", "evening", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "4", 2, "pages", "evening", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(1, "5", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        List<TaskModel> userTaskArray = taskInfoTable.getTasksByTimeOfDay(userTable.getUserId(user.getUsername()), "evening");

        assertEquals(3, userTaskArray.size());;

    }

    @Test
    void GetTaskByTimeOfDay_userHasTwoAllDayTasksMultUsersHaveTasks_ListOfLengthTwoIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "1", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        UserModel user2 = new UserModel("username2", "0987654321", "gmail@gmail.com");
        userTable.addUser(user2);

        task = new TaskModel(userTable.getUserId(user2.getUsername()), "1", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(userTable.getUserId(user2.getUsername()), "2", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);


        List<TaskModel> userTaskArray = taskInfoTable.getTasksByTimeOfDay(userTable.getUserId(user2.getUsername()), "all day");

        assertEquals(2, userTaskArray.size());;

    }



    @Test
    void GetSingleTask_TaskDoesNotExist_NullIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel userTask = taskInfoTable.getSingleTask(userTable.getUserId(user.getUsername()), "read");

        assertNull(userTask);

    }

    @Test
    void GetSingleTask_UserDoesNotExist_NullIsReturned() throws SQLException{

        TaskModel userTask = taskInfoTable.getSingleTask(1, "read");

        assertNull(userTask);

    }

    @Test
    void GetSingleTask_TaskExists_NullIsNotReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "walk", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel userTask = taskInfoTable.getSingleTask(1, "walk");

        assertNotNull(userTask);

    }

    @Test
    void GetSingleTask_TaskExists_CorrectTaskObjectIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "walk", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel userTask = taskInfoTable.getSingleTask(1, "walk");

        assertEquals(userTable.getUserId(user.getUsername()), userTask.getCorrespondingUserId());
        assertEquals("walk", userTask.getTaskName());
        assertEquals(2, userTask.getTaskGoalAmount());
        assertEquals("pages", userTask.getTaskUnit());
        assertEquals("all day", userTask.getTimeOfDay());
        assertEquals("01-09-2026", userTask.getStartDate());
        assertEquals("12-21-2026", userTask.getEndDate());

    }

    @Test
    void GetSingleTask_TaskExistsMultTasks_CorrectTaskObjectIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "walk", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 3, "chapters", "evening", "02-09-2026", "11-21-2026");
        taskInfoTable.addTask(task);

        TaskModel userTask = taskInfoTable.getSingleTask(1, "read");

        assertEquals(userTable.getUserId(user.getUsername()), userTask.getCorrespondingUserId());
        assertEquals("read", userTask.getTaskName());
        assertEquals(3, userTask.getTaskGoalAmount());
        assertEquals("chapters", userTask.getTaskUnit());
        assertEquals("evening", userTask.getTimeOfDay());
        assertEquals("02-09-2026", userTask.getStartDate());
        assertEquals("11-21-2026", userTask.getEndDate());

    }

    @Test
    void GetSingleTask_TaskWalkDoesNotExist_NullIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel userTask = taskInfoTable.getSingleTask(userTable.getUserId(user.getUsername()), "walk");

        assertNull(userTask);

    }


    @Test
    void GetTaskId_TaskDoesNotExist_NegativeOneIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        int taskId = taskInfoTable.getTaskId(1, "read");

        assertEquals(-1, taskId);

    }

    @Test
    void GetTaskId_TaskDoesExist_OneIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "walk", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        int taskId = taskInfoTable.getTaskId(task.getCorrespondingUserId(), task.getTaskName());

        assertEquals(1, taskId);

    }
}
