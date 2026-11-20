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
            + "VALUES (1, 'read', 2, 'pages', 'morning', '01-09-2026', '12-21-2026')";
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.executeUpdate();
            }
        }
    }

}
