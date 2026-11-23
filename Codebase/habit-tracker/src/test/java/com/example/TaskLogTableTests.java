package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

/**
 * TaskLogTableTests - Test file for the TaskLogTable class.
 */
public class TaskLogTableTests {

    private String dbFile = "habitTrackerTestTemp.db";
    private DatabaseSetup dbSetup;
    private TaskInfoTable taskInfoTable;
    private UserTable userTable;
    private TaskLogTable taskLogTable;

    

    // Creates a brand new temp database for each test
    @TempDir
    Path tempDir;

    @BeforeEach 
    void init() throws Exception {
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
        dbSetup.createTables();
        taskInfoTable = new TaskInfoTable(dbSetup);
        userTable = new UserTable(dbSetup);
        taskLogTable = new TaskLogTable(dbSetup);
    }



    @Test
    void AddTaskLog_FirstLog_OneRowInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "12-12-2012", true);
        taskLogTable.addLog(taskLog);

        String query = "SELECT COUNT(*) FROM taskLogTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_FirstLog_LogInDbIsCorrect() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        String query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_LogAlreadyExistsForThatDay_OnlyOneRowInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-12-2026", false);
        taskLogTable.addLog(taskLog2);

        String query = "SELECT COUNT(*) FROM taskLogTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_LogAlreadyExistsForThatDay_DbIsUpdated() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        String query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-12-2026", false);
        taskLogTable.addLog(taskLog2);

        query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = false;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_DuplicateIdsDiffDay_TwoRowsInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-13-2026", true);
        taskLogTable.addLog(taskLog2);

        String query = "SELECT COUNT(*) FROM taskLogTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_DuplicateIdsDiffDay_BothLogsInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-13-2026", true);
        taskLogTable.addLog(taskLog2);

        String query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-13-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_DupeDateDiffUserId_BothLogsInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);
        int userId2 = userTable.getUserId(user2.getUsername());


        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskModel task2 = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task2);
        int taskId2 = taskInfoTable.getTaskId(userId2, task2.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        TaskLogModel taskLog2 = new TaskLogModel(userId2, taskId2, "01-12-2026", true);
        taskLogTable.addLog(taskLog2);


        String query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 2 AND taskId = 2 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddTaskLog_DupeUserIdDupeDateDiffTaskId_BothLogsInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskModel task2 = new TaskModel(1, "walk", 2, "pages", "morning", "01-09-2026", "01-21-2026");
        taskInfoTable.addTask(task2);
        int taskId2 = taskInfoTable.getTaskId(userId, task2.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId2, "01-12-2026", true);
        taskLogTable.addLog(taskLog2);


        String query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 1 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM taskLogTable WHERE userId = 1 AND taskId = 2 AND taskLogDate = '01-12-2026' AND isTaskComplete = true;";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }
    


    @Test
    void DeleteTaskLog_FirstTask_ReturnsOne() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(1, 1, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        int result = taskLogTable.deleteTaskLog(userId, taskId, "01-12-2026");

        assertEquals(1, result);
    }

    @Test
    void DeleteTaskLog_TaskLogDoesNotExist_ReturnsZero() throws SQLException{
        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        int result = taskLogTable.deleteTaskLog(userId, taskId, "01-12-2026");

        assertEquals(0, result);
    }

    @Test
    void DeleteTaskLog_TwoTasksInTable_OneRowInTable() throws SQLException{
        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);
        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-13-2026", true);
        taskLogTable.addLog(taskLog2);

        taskLogTable.deleteTaskLog(userId, taskId, "01-12-2026");

        String query = "SELECT COUNT(*) FROM taskLogTable";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void DeleteTaskLog_TwoTasksInTableWithSameDate_OneRowInTable() throws SQLException{
        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());


        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskModel task2 = new TaskModel(1, "walk", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task2);
        int taskId2 = taskInfoTable.getTaskId(userId, task2.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);
        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId2, "01-12-2026", true);
        taskLogTable.addLog(taskLog2);

        taskLogTable.deleteTaskLog(userId, taskId, "01-12-2026");

        String query = "SELECT COUNT(*) FROM taskLogTable";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void DeleteTaskLog_DeletedMultTaskLogs_ZeroRowsInTable() throws SQLException{
        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);
        int userId2 = userTable.getUserId(user2.getUsername());


        // If a task is not added the foreign key constraint will fail
        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskModel task2 = new TaskModel(2, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task2);
        int taskId2 = taskInfoTable.getTaskId(userId2, task2.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);
        TaskLogModel taskLog2 = new TaskLogModel(userId2, taskId2, "01-12-2026", true);
        taskLogTable.addLog(taskLog2);

        taskLogTable.deleteTaskLog(userId, taskId, "01-12-2026");
        taskLogTable.deleteTaskLog(userId2, taskId2, "01-12-2026");

        String query = "SELECT COUNT(*) FROM taskLogTable";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }
    }



    @Test
    void GetTaskLogsByUserIdAndDate_UserDoesNotExist_EmptyListIsReturned() throws SQLException{

        List<TaskLogModel> userTaskLogList = taskLogTable.getTaskLogsByDateAndUserId(1, "01-12-2026");

        assertTrue(userTaskLogList.isEmpty());

    }

    @Test
    void GetTaskLogsByUserIdAndDate_userHasOneLogOnThatDate_ListOfLengthOneIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        List<TaskLogModel> userTaskLogList = taskLogTable.getTaskLogsByDateAndUserId(1, "01-12-2026");

        assertEquals(1, userTaskLogList.size());;

    }



    @Test
    void GetAllLogsForTask_UserDoesNotExist_EmptyListIsReturned() throws SQLException{

        List<TaskLogModel> userTaskLogList = taskLogTable.getAllLogsForTask(1, 1);

        assertTrue(userTaskLogList.isEmpty());

    }

    @Test
    void GetAllLogsForTask_userHasOneLog_ListOfLengthOneIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        List<TaskLogModel> userTaskLogList = taskLogTable.getAllLogsForTask(userId, taskId);

        assertEquals(1, userTaskLogList.size());;

    }

    @Test
    void GetAllLogsForTask_userHasTwoLogsForATask_ListOfLengthTwoIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);
        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-13-2026", true);
        taskLogTable.addLog(taskLog2);

        List<TaskLogModel> userTaskLogList = taskLogTable.getAllLogsForTask(userId, taskId);

        assertEquals(2, userTaskLogList.size());;

    }

    @Test
    void GetAllLogsForTask_userHasLogsForDiffTasks_ListOfLengthTwoIsReturned() throws SQLException{
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());


        TaskModel task = new TaskModel(1, "read", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskModel task2 = new TaskModel(1, "write", 2, "pages", "morning", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task2);
        int taskId2 = taskInfoTable.getTaskId(userId, task2.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);
        TaskLogModel taskLog2 = new TaskLogModel(userId, taskId, "01-13-2026", true);
        taskLogTable.addLog(taskLog2);
        TaskLogModel taskLog3 = new TaskLogModel(userId, taskId2, "01-13-2026", true);
        taskLogTable.addLog(taskLog3);

        List<TaskLogModel> userTaskLogList = taskLogTable.getAllLogsForTask(userId, taskId);

        assertEquals(2, userTaskLogList.size());;

    }


    @Test
    void GetTaskLogId_TaskLogDoesNotExist_NegativeOneIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        int taskLogId = taskLogTable.getTaskLogId(userId, taskId, "01-12-2026");

        assertEquals(-1, taskLogId);

    }

    @Test
    void GetTaskLogId_TaskLogDoesExist_OneIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);
        int taskId = taskInfoTable.getTaskId(userId, task.getTaskName());

        TaskLogModel taskLog = new TaskLogModel(userId, taskId, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        int taskLogId = taskLogTable.getTaskLogId(userId, taskId, taskLog.getTaskLogDate());

        assertEquals(1, taskLogId);

    }

    @Test
    void GetTaskLogId_TaskLogDoesExist_LogIdIsReturnedNotTaskId() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());


        TaskModel task = new TaskModel(userTable.getUserId(user.getUsername()), "read", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task);

        TaskModel task2 = new TaskModel(userTable.getUserId(user.getUsername()), "walk", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task2);

        TaskModel task3 = new TaskModel(userTable.getUserId(user.getUsername()), "run", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task3);

        TaskModel task4 = new TaskModel(userTable.getUserId(user.getUsername()), "bike", 2, "pages", "all day", "01-09-2026", "12-21-2026");
        taskInfoTable.addTask(task4);
        int taskId4 = taskInfoTable.getTaskId(userId, task4.getTaskName());


        TaskLogModel taskLog = new TaskLogModel(userId, taskId4, "01-12-2026", true);
        taskLogTable.addLog(taskLog);

        int taskLogId = taskLogTable.getTaskLogId(userId, taskId4, taskLog.getTaskLogDate());

        // Should not equal 4
        assertEquals(1, taskLogId);

    }

    

    

}
