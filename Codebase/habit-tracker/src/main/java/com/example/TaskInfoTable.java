package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public int deleteTask(int userId, String taskName) throws SQLException{
        String sql = "DELETE FROM genTaskTable WHERE userId = ? AND taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setString(2, taskName);
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

    public int updateTaskName(int userId, String originalName, String newName) throws SQLException{
        String sql = "UPDATE genTaskTable set taskName = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newName);
                ps.setString(2, originalName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateTaskGoalAmount(int userId, String taskName, int newGoalAmount) throws SQLException{
        String sql = "UPDATE genTaskTable set taskGoal = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, newGoalAmount);
                ps.setString(2, taskName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateTaskUnit(int userId, String taskName, String newUnit) throws SQLException{
        String sql = "UPDATE genTaskTable set taskUnit = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newUnit);
                ps.setString(2, taskName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateTimeOfDay(int userId, String taskName, String newTimeOfDay) throws SQLException{
        String sql = "UPDATE genTaskTable set taskTimeOfDay = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newTimeOfDay);
                ps.setString(2, taskName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateTaskStartDate(int userId, String taskName, String newStartDate) throws SQLException{
        String sql = "UPDATE genTaskTable set taskStartDate = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newStartDate);
                ps.setString(2, taskName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateTaskEndDate(int userId, String taskName, String newEndDate) throws SQLException{
        String sql = "UPDATE genTaskTable set taskEndDate = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newEndDate);
                ps.setString(2, taskName);
                ps.setInt(3, userId);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public List<TaskModel> getTasksByUserId(int userId) throws SQLException{

        List<TaskModel> userTaskList = new ArrayList<>();

        String sql = "SELECT * FROM genTaskTable WHERE userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {             
                        return userTaskList;
                    }
                    else{
                        do{
                            int correspondingUserId = rs.getInt("userId");
                            String taskName = rs.getString("taskName");
                            int taskGoalAmount = rs.getInt("taskGoal");
                            String taskUnit = rs.getString("taskUnit");
                            String timeOfDay = rs.getString("taskTimeOfDay");
                            String startDate = rs.getString("taskStartDate");
                            String endDate = rs.getString("taskEndDate");

                            TaskModel newTask = new TaskModel(correspondingUserId, taskName, taskGoalAmount, taskUnit, timeOfDay, startDate, endDate);
                            userTaskList.add(newTask);
                        }
                        while(rs.next());
                    }                          
                }

                return userTaskList;
            }
        }
    }

    public List<TaskModel> getTasksByTimeOfDay(int userId, String timeOfDaySearch) throws SQLException{

        List<TaskModel> userTaskList = new ArrayList<>();

        String sql = "SELECT * FROM genTaskTable WHERE userId = ? AND taskTimeOfDay = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setString(2, timeOfDaySearch);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {             
                        return userTaskList;
                    }
                    else{
                        do{
                            int correspondingUserId = rs.getInt("userId");
                            String taskName = rs.getString("taskName");
                            int taskGoalAmount = rs.getInt("taskGoal");
                            String taskUnit = rs.getString("taskUnit");
                            String timeOfDay = rs.getString("taskTimeOfDay");
                            String startDate = rs.getString("taskStartDate");
                            String endDate = rs.getString("taskEndDate");

                            TaskModel newTask = new TaskModel(correspondingUserId, taskName, taskGoalAmount, taskUnit, timeOfDay, startDate, endDate);
                            userTaskList.add(newTask);
                        }
                        while(rs.next());
                    }                          
                }

                return userTaskList;
            }
        }
    }

    public TaskModel getSingleTask(int userId, String taskNameSearch) throws SQLException{ 
   
        String sql = "SELECT * FROM genTaskTable WHERE userId = ? AND taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setString(2, taskNameSearch);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {             
                        int correspondingUserId = rs.getInt("userId");
                        String taskName = rs.getString("taskName");
                        int taskGoalAmount = rs.getInt("taskGoal");
                        String taskUnit = rs.getString("taskUnit");
                        String timeOfDay = rs.getString("taskTimeOfDay");
                        String startDate = rs.getString("taskStartDate");
                        String endDate = rs.getString("taskEndDate");

                        TaskModel task = new TaskModel(correspondingUserId, taskName, taskGoalAmount, taskUnit, timeOfDay, startDate, endDate);
                        
                        return task;
                    }
                    else{
                        return null;
                    }
                    
                }                          
            }
        }
    }

}
