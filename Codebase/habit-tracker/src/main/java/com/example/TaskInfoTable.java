package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TaskInfoTable {

    private DatabaseSetup db;

    public TaskInfoTable(DatabaseSetup db){
        this.db = db;
    }

    public void addTask(TaskModel task) throws SQLException{
        String sql = "INSERT INTO genTaskTable (userId, taskName, taskGoal, taskUnit, taskTimeOfDay, taskStartDate, taskEndDate) " 
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, task.getCorrespondingUserId());
                ps.setString(2, task.getTaskName());
                ps.setInt(3, task.getTaskGoalAmount());
                ps.setString(4, task.getTaskUnit());
                ps.setString(5, task.getTimeOfDay());
                ps.setString(6, task.getStartDate());
                ps.setString(7, task.getEndDate());
                ps.executeUpdate();
            }
        }
    }

    public int deleteTask(String taskName) throws SQLException{
        String sql = "DELETE FROM genTaskTable WHERE taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, taskName);
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

}
