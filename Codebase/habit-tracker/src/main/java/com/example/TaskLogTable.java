package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<TaskLogModel> getTaskLogsByDateAndUserId(int userId, String searchDate) throws SQLException{
        List<TaskLogModel> taskLogList = new ArrayList<>();

        String sql = "SELECT * FROM taskLogTable WHERE userId = ? AND taskLogDate = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setString(2, searchDate);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {             
                        return taskLogList;
                    }
                    else{
                        do{
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

    public List<TaskLogModel> getAllLogsForTask(int userId, int taskId) throws SQLException{
        List<TaskLogModel> taskLogList = new ArrayList<>();

        String sql = "SELECT * FROM taskLogTable WHERE userId = ? AND taskId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setInt(2, taskId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {             
                        return taskLogList;
                    }
                    else{
                        do{
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



}
