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

    public int deleteTaskLog(int userId, int taskId, String taskDate) throws SQLException{
        String query = "DELETE FROM taskLogTable WHERE taskLogDate = '01-12-2026' AND taskId = 1 AND userId = 1;";
        try (Connection conn = db.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);){

            int numRowsDeleted = ps.executeUpdate();

            return numRowsDeleted;
        }
    }



}
