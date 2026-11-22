package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskLogTable {

    private DatabaseSetup db;

    public TaskLogTable(DatabaseSetup db){
        this.db = db;
    }

    public void addLog(TaskLogModel taskLog) throws SQLException{

        String sql = "INSERT OR REPLACE INTO taskLogTable (userId, taskId, taskLogDate, isTaskComplete) " 
            + "VALUES (?, ?, ?, ?)";
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, taskLog.getCorrespondingUserId());
                ps.setInt(2, taskLog.getCorrespondingTaskId());
                ps.setString(3, taskLog.getTaskLogDate());
                ps.setBoolean(4, taskLog.getTaskCompletionStatus());
                ps.executeUpdate();
            }
        }
    }

    public int deleteTaskLog(int userId, int taskId, String taskLogDate) throws SQLException{

        String sql = "DELETE FROM taskLogTable WHERE userId = ? AND taskId = ? AND taskLogDate = ?;";
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setInt(2, taskId);
                ps.setString(3, taskLogDate);
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }



}
