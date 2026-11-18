package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserTable {
    private Connection conn;

    public UserTable(Connection conn){
        this.conn = conn;
    }

    public int addUser(UserModel user) throws SQLException{
        String sql = "INSERT INTO userTable (username, passwordHash, email) VALUES (?, ?, ?)";

        try(PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getUserEmail());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()){
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public int deleteUser(String username) throws SQLException{
        String sql = "DELETE FROM userTable WHERE username = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, username);
            int numRowsDeleted = ps.executeUpdate();

            return numRowsDeleted;
        }
    }

    public int updateUsername(String oldUsername, String newUsername) throws SQLException{
        String sql = "UPDATE userTable set username = ? WHERE username = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, newUsername);
            ps.setString(2, oldUsername);
            int numRowsUpdated = ps.executeUpdate();

            return numRowsUpdated;
        }
    }

    public int updatePasswordHash(String username, String newPasswordHash) throws SQLException{
        String sql = "UPDATE userTable set passwordHash = ? WHERE username = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, newPasswordHash);
            ps.setString(2, username);
            int numRowsUpdated = ps.executeUpdate();

            return numRowsUpdated;
        }
    }

    public int updateEmail(String username, String newEmail) throws SQLException{
        String sql = "UPDATE userTable set email = ? WHERE username = ?";

        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, newEmail);
            ps.setString(2, username);
            int numRowsUpdated = ps.executeUpdate();

            return numRowsUpdated;
        }
    }


    // ------------ returns null if user not found in table???????????????? ----------------
    public UserModel getUserByUsername(String username) throws SQLException{
        String sql = "SELECT * FROM userTable WHERE username = ?";

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
