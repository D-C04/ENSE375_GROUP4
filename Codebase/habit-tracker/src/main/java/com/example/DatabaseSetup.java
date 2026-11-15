package com.example;

import java.sql.Statement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseSetup {

    private final String url;

    /**
     * Sets up the database file path and URL.
     * Creates a directory called '.habittracker' in the user's home directory to store .db files.
     * Use this constructor in the main program code.
     * For tests, use {@link #DatabaseSetup(Path, String)}.
     * 
     * @param dbFile the file name for where the db data will be stored
     * @throws IOException if the directory cannot be created
     */
    public DatabaseSetup(String dbFile) throws IOException{

        // Creates a directory to store the local db data
        Path dir = Path.of(System.getProperty("user.home"), ".habittracker");
        Files.createDirectories(dir);
        // When the connection is given this url, it will create an sqlite database at the given file path
        this.url = "jdbc:sqlite:" + dir.resolve(dbFile);

    }

    /**
     * Sets up the test database file path and URL.
     * Use this constructor only for tests!
     * For Path, give it {@code @TempDir}
     * For the main program, use {@link #DatabaseSetup(String)}.
     * 
     * @param dir the directory to store the .db files in
     * @param dbFile the file name for where the db data will be stored
     * @throws IOException if the directory cannot be created
     */
    public DatabaseSetup(Path dir, String dbFile) throws IOException {
        Files.createDirectories(dir);
        this.url = "jdbc:sqlite:" + dir.resolve(dbFile);
    }

    /**
     * Establishes a connection to the database and enables Foreign Keys.
     * The caller must close the connection.
     * 
     * @return an open connection to the database
     * @throws SQLException if the connection cannot be opened
     */
    public Connection openConnection() throws SQLException {
        
        // Attempts a connection to the database
        // If the dbFile does not yet exist, it will create it
        Connection conn = DriverManager.getConnection(url);

        try(Statement s = conn.createStatement()){
            // Enables foreign keys: allows tables to be linked together and for cascade deletion
            s.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) { // Closes the connection if the execute fails
            conn.close();
            throw e;
        }

        return conn;
    }

    /**
     * Creates the database tables: userTable, genTaskTable, taskLogTable, noteTable.
     * userTable: Holds user specific info, such as username, passwordHash, and email.
     * genTaskTable: Holds data that defines each task. If a user is deleted, their tasks get deleted.
     * taskLogTable: Holds info related if and when tasks were completed.
     * noteTable: Holds the user written notes and day they were written on. If a user is deleted, their notes get deleted.
     * 
     * @throws SQLException if at least one table cannot be created. If one fails, the ones above it will still be created.
     */
    public void createTables() throws SQLException {
        try (Connection conn = openConnection(); Statement s = conn.createStatement()) {

            // User Table
            String sqlCreateUserTable = """
                CREATE TABLE IF NOT EXISTS `userTable` ( `userId` integer not null primary key autoincrement, `username` 
                TEXT not null, `passwordHash` TEXT not null, `email` TEXT not null, UNIQUE (username), UNIQUE (email) ) STRICT
            """;
            s.execute(sqlCreateUserTable);

            // General Task Table
            String sqlCreateGenTaskTable = """
                CREATE TABLE IF NOT EXISTS `genTaskTable` ( `taskId` integer not null primary key autoincrement, `userId`
                 INTEGER not null, `taskName` TEXT not null, `taskGoal` INT not null, `taskUnit` TEXT not null, `taskTimeOfDay` 
                 TEXT not null, `taskStartDate` TEXT not null, `taskEndDate` TEXT null, `taskIcon` TEXT not null, 
                 FOREIGN KEY (userId) REFERENCES userTable(userId) ON DELETE CASCADE ) STRICT
            """;
            s.execute(sqlCreateGenTaskTable);

            // Task Log Table
            String sqlCreateTaskLogTable = """
                CREATE TABLE IF NOT EXISTS `taskLogTable` ( `taskLogId` integer not null primary key autoincrement, `taskId` 
                INTEGER not null, `userId` INTEGER not null, `taskLogDate` TEXT not null, `isTaskComplete` INT not null, 
                FOREIGN KEY (userId) REFERENCES userTable(userId) ON DELETE CASCADE, FOREIGN KEY (taskId) REFERENCES
                genTaskTable(taskId)) STRICT
            """;
            s.execute(sqlCreateTaskLogTable);

            // Note Table
            String sqlCreateNoteTable = """
                CREATE TABLE IF NOT EXISTS `noteTable` ( `noteId` integer not null primary key autoincrement,`userId` INTEGER not null, 
                `noteText` TEXT not null, `noteDate` TEXT not null, UNIQUE (userId, noteDate), FOREIGN KEY (userId) REFERENCES userTable(userId)
                 ON DELETE CASCADE) STRICT
            """;
            s.execute(sqlCreateNoteTable);
        }
    }

}
