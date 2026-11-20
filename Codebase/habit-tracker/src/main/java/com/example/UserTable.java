package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserTable {
    private DatabaseSetup db;

    public UserTable(DatabaseSetup db){
        this.db = db;
    }

    public void addUser(UserModel user) throws SQLException{
        String sql = "INSERT INTO userTable (username, passwordHash, email) VALUES (?, ?, ?)";
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getUserEmail());
                ps.executeUpdate();
            }
        }
    }

    public int deleteUser(String username) throws SQLException{
        String sql = "DELETE FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, username);
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

    public int updateUsername(String oldUsername, String newUsername) throws SQLException{
        String sql = "UPDATE userTable set username = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newUsername);
                ps.setString(2, oldUsername);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updatePasswordHash(String username, String newPasswordHash) throws SQLException{
        String sql = "UPDATE userTable set passwordHash = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newPasswordHash);
                ps.setString(2, username);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public int updateEmail(String username, String newEmail) throws SQLException{
        String sql = "UPDATE userTable set email = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, newEmail);
                ps.setString(2, username);
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    public UserModel getUserByUsername(String username) throws SQLException{
        String sql = "SELECT * FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {             
                        String name   = rs.getString("username");
                        String passwordHash   = rs.getString("passwordHash");
                        String email  = rs.getString("email");
                        return new UserModel(name, passwordHash, email);
                    }
                    return null;                              
                }
            }
        }
    }

    public boolean doesUsernameExist(String username) throws SQLException{
        String sql = "SELECT COUNT(*) FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0){
                        return true;
                    }
                    
                    return false;                              
                }
            }
        }
    }

    public boolean doesEmailExist(String email) throws SQLException{
        String sql = "SELECT COUNT(*) FROM userTable WHERE email = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) > 0){
                        return true;
                    }
                    
                    return false;                              
                }
            }
        }
    }

    public int getUserId(String username) throws SQLException{
        String sql = "SELECT userId FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()){
                        int userId = rs.getInt(1);
                    
                        return userId;   
                    }

                    return -1;                             
                }
            }
        }
    }


}
