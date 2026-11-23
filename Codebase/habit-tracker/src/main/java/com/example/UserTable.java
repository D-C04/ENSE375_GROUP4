package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserTable - Holds methods used to access the user database table.
 * All methods in this class use prepared statements to prevent SQL injection.
 * Methods open and close their own database connections.
 * 
 * userTable columns in order: userId (primary key),
 *                             username (text, unique, not null),
 *                             passwordHash (text, not null),
 *                             email (text, unique, not null)
 */
public class UserTable {
    private DatabaseSetup db;

    public UserTable(DatabaseSetup db){
        this.db = db;
    }

    /**
     * Adds the info in the given user model to the database.
     * For a successful add, the username and email must not already appear in the database.
     * If the username or email already exists, the database will throw an exception.
     * 
     * @param user the user model to add to the database
     * @throws SQLException if the username or email already exists in the database
     *                      or if the database cannot be accessed
     */
    public void addUser(UserModel user) throws SQLException{

        // The prepared statement is used to prevent SQL injection.
        // The ? are placeholders for the values that will be put into the database.
        String sql = "INSERT INTO userTable (username, passwordHash, email) VALUES (?, ?, ?)";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                // Sets the values for the placeholders in the prepared statement
                ps.setString(1, user.getUsername()); // Sets the username as the first ? in the sql string
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getUserEmail());

                // Executes the prepared SQL statement
                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes the info tied to the given username from the database.
     * This will cause all tasks, taskLogs, and notes tied to the user to be deleted as well.
     * 
     * @param username the username of the user to delete
     * 
     * @return The number of deleted rows from the user table. 
     *         0, if the username did not exist.
     *         1, if the username was found and deleted.
     *         Should never be greater than one, since usernames are unique.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int deleteUser(String username) throws SQLException{

        String sql = "DELETE FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, username);

                // Executes the prepared SQL statement and returns the amount of deleted rows.
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

    /**
     * Updates the username of the user belonging to the old username to the new username.
     * Does not change any other user information. 
     * Does not create a new table entry, just changes the existing one.
     * 
     * @param oldUsername the current username of the user
     * @param newUsername the new username for the user
     * 
     * @return The number of updated rows from the user table.
     *         0, if the username did not exist.
     *         1, if the username was found and updated.
     *         Should never be greater than one, since usernames are unique.
     * 
     * @throws SQLException if the new username already exists in the database
     *                      or if the database cannot be accessed
     */
    public int updateUsername(String oldUsername, String newUsername) throws SQLException{

        String sql = "UPDATE userTable set username = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                
                ps.setString(1, newUsername);
                ps.setString(2, oldUsername);

                // Executes the prepared SQL statement and returns the amount of updated rows.
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    /**
     * Updates the password hash belonging to the given username to the new password hash.
     * Does not change any other user information. 
     * Does not create a new table entry, just changes the existing one.
     * 
     * @param username the username of the user
     * @param newPasswordHash the new password hash for the user
     * 
     * @return The number of updated rows from the user table.
     *         0, if the username did not exist.
     *         1, if the username was found and the password hash updated.
     *         Should never be greater than one, since usernames are unique.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int updatePasswordHash(String username, String newPasswordHash) throws SQLException{

        String sql = "UPDATE userTable set passwordHash = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, newPasswordHash);
                ps.setString(2, username);

                // Executes the prepared SQL statement and returns the amount of updated rows.
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    /**
     * Updates the email belonging to the given username to the new email.
     * Does not change any other user information. 
     * Does not create a new table entry, just changes the existing one.
     * 
     * @param username the username of the user
     * @param newEmail the new email for the user
     * 
     * @return The number of updated rows from the user table.
     *         0, if the username did not exist.
     *         1, if the username was found and the email updated.
     *         Should never be greater than one, since usernames are unique.
     * 
     * @throws SQLException if the email already exists in the database or if the database cannot be accessed
     */
    public int updateEmail(String username, String newEmail) throws SQLException{

        String sql = "UPDATE userTable set email = ? WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, newEmail);
                ps.setString(2, username);

                // Executes the prepared SQL statement and returns the amount of updated rows.
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    /**
     * Returns a single userModel filled with the info belonging to the given username.
     * The userModel will contain the username, password hash, and email.
     * 
     * @param username the username of the user
     * 
     * @return The userModel filled with the info belonging to the given username.
     *         null, if the username did not exist.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public UserModel getUserByUsername(String username) throws SQLException{

        String sql = "SELECT * FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, username);

                // rs holds the query results. It will be empty if the username was not found in the database.
                try (ResultSet rs = ps.executeQuery()) {

                    // rs must be moved next because it starts before the first row of data.
                    // If you do not call rs.next(), you will not be able to read the data.
                    if (rs.next()) { // will be true if the username exists in the database  

                        // Creates a new userModel with the info retrieved from the database and returns it.
                        String name = rs.getString("username"); // The strings in the quotes must match the column names in the table
                        String passwordHash = rs.getString("passwordHash");
                        String email = rs.getString("email");
                        return new UserModel(name, passwordHash, email);

                    }
                    return null;                              
                }
            }
        }
    }

    /**
     * Checks if the given username exists in the database.
     * 
     * @param username the username to check for in the database
     * 
     * @return true if the username exists in the database, false otherwise
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public boolean doesUsernameExist(String username) throws SQLException{

        String sql = "SELECT COUNT(*) FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    // The first result of the query is the number of times the username appears in the database.
                    // If it is greater than 0, the username already exists.
                    if (rs.getInt(1) > 0){ 
                        return true;
                    }
                    
                    return false;                              
                }
            }
        }
    }

    /**
     * Checks if the given email exists in the database.
     * 
     * @param email the email to check for in the database
     * 
     * @return true if the email exists in the database, false otherwise
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public boolean doesEmailExist(String email) throws SQLException{

        String sql = "SELECT COUNT(*) FROM userTable WHERE email = ?";


        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, email);

                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();

                    // The first result of the query is the number of times the email appears in the database.
                    // If it is greater than 0, the email already exists.
                    if (rs.getInt(1) > 0){
                        return true;
                    }
                    
                    return false;                              
                }
            }
        }
    }

    /**
     * Gets the user ID for the given username.
     * 
     * @param username the username to search for in the database
     * 
     * @return the user ID if the username exists in the database, -1 otherwise
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int getUserId(String username) throws SQLException{

        String sql = "SELECT userId FROM userTable WHERE username = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {
                    if(rs.next()){

                        // The first result of the query is the user ID.
                        int userId = rs.getInt(1);
                    
                        return userId;   
                    }

                    return -1;                             
                }
            }
        }
    }

}
