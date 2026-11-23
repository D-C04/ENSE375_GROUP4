package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskLogTable - Holds methods used to access the task log database table.
 * All methods in this class use prepared statements to prevent SQL injection.
 * Methods open and close their own database connections.
 * 
 * taskLogTable columns in order: taskLogId (primary key, auto-increment), -- Do not insert values into this one
 *                                userId (foreign key, references userTable.userId),
 *                                taskId (foreign key, references taskInfoTable.taskId),
 *                                taskLogDate (text, unique with userId and taskId, not null),
 *                                isTaskComplete (boolean, not null)
 */
public class TaskLogTable {

    private DatabaseSetup db;

    public TaskLogTable(DatabaseSetup db){
        this.db = db;
    }

    /**
     * Adds the info in the given task log model to the database.
     * If the userId, taskId, and taskLogDate already exist in the database, 
     * the new log will replace the old log.
     * 
     * @param taskLog the task log model to add to the database
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public void addLog(TaskLogModel taskLog) throws SQLException{

        // The prepared statement is used to prevent SQL injection.
        // The ? are placeholders for the values that will be put into the database.
        String sql = "INSERT OR REPLACE INTO taskLogTable (userId, taskId, taskLogDate, isTaskComplete) " 
            + "VALUES (?, ?, ?, ?)";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                // Sets the values for the placeholders in the prepared statement
                ps.setInt(1, taskLog.getCorrespondingUserId()); // Sets the userId as the first ? in the sql string
                ps.setInt(2, taskLog.getCorrespondingTaskId());
                ps.setString(3, taskLog.getTaskLogDate());
                ps.setBoolean(4, taskLog.getTaskCompletionStatus());

                // Executes the prepared SQL statement
                ps.executeUpdate();
            }
        }
    }


    /**
     * Deletes the log info tied to the given userId and taskId from the database.
     * 
     * @param userId the ID of the user whose log info to delete
     * @param taskId the ID of the task whose log info to delete
     * @param taskLogDate the date of the task log to delete
     * 
     * @return The number of deleted rows from the taskLogTable. 
     *         0, if the log was not found
     *         1, if the log was deleted.
     *         Should never be greater than one, since logs are unique for userId and taskId.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int deleteTaskLog(int userId, int taskId, String taskLogDate) throws SQLException{

        String sql = "DELETE FROM taskLogTable WHERE userId = ? AND taskId = ? AND taskLogDate = ?;";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setInt(2, taskId);
                ps.setString(3, taskLogDate);

                // Executes the prepared SQL statement and returns the amount of deleted rows.
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

    /**
     * Gets all the task logs belonging to the given userId for the given date.
     * 
     * @param userId the userId for the user whose task logs to get
     * @param searchDate the date to filter task logs by
     * 
     * @return A list of TaskLogModel objects filled with task log info from every single task log belonging to the given userId
     *         for the given date.
     *         An empty list, if no task logs were found for the given userId and date.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public List<TaskLogModel> getTaskLogsByDateAndUserId(int userId, String searchDate) throws SQLException{

        // Initializes the list as empty
        List<TaskLogModel> taskLogList = new ArrayList<>();

        String sql = "SELECT * FROM taskLogTable WHERE userId = ? AND taskLogDate = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setString(2, searchDate);

                try (ResultSet rs = ps.executeQuery()) {

                    // rs holds the query results.
                    // If no task logs were found rs will be empty, so return the empty list.
                    if (!rs.next()) {             
                        return taskLogList;
                    }
                    else{
                        do{
                            // Create a new TaskLogModel object for each task log found and adds it to the list.
                            int correspondingUserId = rs.getInt("userId");
                            int correspondingTaskId = rs.getInt("taskId");
                            String date = rs.getString("taskLogDate");
                            boolean isComplete = rs.getBoolean("isTaskComplete");

                            TaskLogModel newTaskLog = new TaskLogModel(correspondingUserId, correspondingTaskId, date, isComplete);
                            taskLogList.add(newTaskLog);
                        }
                        while(rs.next());
                    }                          
                }

                return taskLogList;
            }
        }
    }

    /**
     * Gets all the task logs belonging to the given userId for the given taskId.
     * 
     * @param userId the userId for the user whose task logs to get
     * @param taskId the taskId for the task whose logs to get
     * 
     * @return A list of TaskLogModel objects filled with task log info from every single task log belonging to the given userId
     *         for the given taskId.
     *         An empty list, if no task logs were found for the given userId and taskId.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public List<TaskLogModel> getAllLogsForTask(int userId, int taskId) throws SQLException{

        List<TaskLogModel> taskLogList = new ArrayList<>();

        String sql = "SELECT * FROM taskLogTable WHERE userId = ? AND taskId = ? ORDER BY taskLogDate";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setInt(2, taskId);

                try (ResultSet rs = ps.executeQuery()) {

                    // rs holds the query results.
                    // If no task logs were found rs will be empty, so return the empty list.
                    if (!rs.next()) {             
                        return taskLogList;
                    }
                    else{
                        do{

                            // Create a new TaskLogModel object for each task log found and adds it to the list.
                            int correspondingUserId = rs.getInt("userId");
                            int correspondingTaskId = rs.getInt("taskId");
                            String date = rs.getString("taskLogDate");
                            boolean isComplete = rs.getBoolean("isTaskComplete");

                            TaskLogModel newTaskLog = new TaskLogModel(correspondingUserId, correspondingTaskId, date, isComplete);
                            taskLogList.add(newTaskLog);
                        }
                        while(rs.next());
                    }                          
                }

                return taskLogList;
            }
        }
    }

    /**
     * Gets the task log ID for the given userId, taskId, and date.
     * 
     * @param userId the userId for the user whose taskLogId to get
     * @param taskId the taskId for the task whose taskLogId to get
     * @param date the date of the taskLogId to get
     * 
     * @return the task log ID if the task log exists in the database, -1 otherwise
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int getTaskLogId(int userId, int taskId, String date) throws SQLException{

        String sql = "SELECT taskLogId FROM taskLogTable WHERE userID = ? AND taskId = ? AND taskLogDate = ?;";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setInt(2, taskId);
                ps.setString(3, date);

                try (ResultSet rs = ps.executeQuery()) {

                    // rs must be moved next because it starts before the first row of data.
                    // If you do not call rs.next(), you will not be able to read the data.
                    if(rs.next()){

                        // The first result of the query is the log ID.
                        int taskID = rs.getInt(1);
                    
                        return taskID;   
                    }

                    return -1;                             
                }
            }
        }
    }



}
