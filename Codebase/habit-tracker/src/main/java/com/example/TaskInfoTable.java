package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskInfoTable - Holds methods used to access the task info database table.
 * All methods in this class use prepared statements to prevent SQL injection.
 * Methods open and close their own database connections.
 * 
 * taskInfoTable columns in order: taskId (primary key, auto-increment), -- Do not insert values into this one
 *                                 userId (foreign key, references userTable.userId),
 *                                 taskName (text, unique with userId, not null),
 *                                 taskGoal (integer, not null),
 *                                 taskUnit (text, not null),
 *                                 taskTimeOfDay (text, not null),
 *                                 taskStartDate (text, not null),
 *                                 taskEndDate (text, not null)
 */
public class TaskInfoTable {

    private DatabaseSetup db;

    public TaskInfoTable(DatabaseSetup db){
        this.db = db;
    }

    /**
     * Adds the info in the given task model to the database.
     * For a successful add, the task name must be unique for the given userId.
     * If the task name already exists for that userId, the database will throw an exception.
     * 
     * @param task the task model to add to the database
     * 
     * @throws SQLException if the task name already exists for the userId
     *                      or if the database cannot be accessed
     */
    public void addTask(TaskModel task) throws SQLException{

        // The prepared statement is used to prevent SQL injection.
        // The ? are placeholders for the values that will be put into the database.
        String sql = "INSERT INTO genTaskTable (userId, taskName, taskGoal, taskUnit, taskTimeOfDay, taskStartDate, taskEndDate) " 
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                // Sets the values for the placeholders in the prepared statement
                ps.setInt(1, task.getCorrespondingUserId()); // Sets the userId as the first ? in the sql string
                ps.setString(2, task.getTaskName()); // Must be unique for the given userId
                ps.setInt(3, task.getTaskGoalAmount());
                ps.setString(4, task.getTaskUnit());
                ps.setString(5, task.getTimeOfDay());
                ps.setString(6, task.getStartDate());
                ps.setString(7, task.getEndDate());

                // Executes the prepared SQL statement
                ps.executeUpdate();
            }
        }
    }

    /**
     * Deletes the info tied to the given userId and taskName from the database.
     * This will cause all taskLogs tied to the task to be deleted as well.
     * 
     * @param userId the userId of the user whose task to delete
     * @param taskName the name of the task to delete
     * 
     * @return The number of deleted rows from the task info table.
     *         0, if the task was not found
     *         1, if the task was found and deleted.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int deleteTask(int userId, String taskName) throws SQLException{

        String sql = "DELETE FROM genTaskTable WHERE userId = ? AND taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setString(2, taskName);

                // Executes the prepared SQL statement and returns the amount of deleted rows.
                int numRowsDeleted = ps.executeUpdate();

                return numRowsDeleted;
            }
        }
    }

    /**
     * Updates the task name for the given userId and original task name to the new task name.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param originalName the current task name
     * @param newName the new task name
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the new task name already exists in the database for the given userId
     *                      or if the database cannot be accessed
     */
    public int updateTaskName(int userId, String originalName, String newName) throws SQLException{

        String sql = "UPDATE genTaskTable set taskName = ? WHERE taskName = ? AND userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setString(1, newName);
                ps.setString(2, originalName);
                ps.setInt(3, userId);

                // Executes the prepared SQL statement and returns the amount of updated rows.
                int numRowsUpdated = ps.executeUpdate();

                return numRowsUpdated;
            }
        }
    }

    /**
     * Updates the goal amount for the given userId and task name to the new goal amount.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param taskName the task name for the task to update
     * @param newGoalAmount the new goal amount
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
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

    /**
     * Updates the unit for the given userId and task name to the new unit.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param taskName the task name for the task to update
     * @param newUnit the new unit
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
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

    /**
     * Updates the time of day for the given userId and task name to the new time of day.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param taskName the task name for the task to update
     * @param newTimeOfDay the new time of day
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
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

    /**
     * Updates the start date for the given userId and task name to the new start date.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param taskName the task name for the task to update
     * @param newStartDate the new start date
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
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

    /**
     * Updates the end date for the given userId and task name to the new end date.
     * Does not change any other task information.
     * Does not create a new table entry, just changes the existing one.
     *
     * @param userId the userId of the user whose task to update
     * @param taskName the task name for the task to update
     * @param newEndDate the new end date
     * 
     * @return The number of updated rows from the task info table.
     *         0, if the task was not found.
     *         1, if the task was updated.
     *         Should never be greater than one, since task names are unique for userIds.
     * 
     * @throws SQLException if the database cannot be accessed
     */
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

    /**
     * Gets all the tasks belonging to the given userId.
     * 
     * @param userId the userId for the user whose tasks to get
     * 
     * @return A list of TaskModel objects filled with task info from every single task belonging to the given userId.
     *         An empty list, if no tasks were found for the given userId.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public List<TaskModel> getTasksByUserId(int userId) throws SQLException{

        // Initializes the list as empty
        List<TaskModel> userTaskList = new ArrayList<>();

        String sql = "SELECT * FROM genTaskTable WHERE userId = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {

                    // rs holds the query results.
                    // If no tasks were found rs will be empty, so return the empty list.
                    if (!rs.next()) {             
                        return userTaskList;
                    }
                    else{
                        do{

                            // Creates a new TaskModel object for each task found and adds it to the list.
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

    /**
     * Gets all the tasks belonging to the given userId for the given time of day.
     * 
     * @param userId the userId for the user whose tasks to get
     * @param timeOfDaySearch the time of day to filter tasks by
     * 
     * @return A list of TaskModel objects filled with task info from every single task belonging to the given userId
     *         for the given time of day.
     *         An empty list, if no tasks were found for the given userId.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public List<TaskModel> getTasksByTimeOfDay(int userId, String timeOfDaySearch) throws SQLException{

        // Initializes the list as empty
        List<TaskModel> userTaskList = new ArrayList<>();

        String sql = "SELECT * FROM genTaskTable WHERE userId = ? AND taskTimeOfDay = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setString(2, timeOfDaySearch);

                try (ResultSet rs = ps.executeQuery()) {

                    // If no tasks were found rs will be empty, so return the empty list.
                    if (!rs.next()) {             
                        return userTaskList;
                    }
                    else{
                        do{

                            // Creates a new TaskModel object for each task found and adds it to the list.
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

    /**
     * Gets the task belonging to the given userId with the given task name.
     * 
     * @param userId the userId for the user whose task to get
     * @param taskNameSearch the name of the task to get
     * 
     * @return The TaskModel object filled with the info from the task with the given name
     *         and belonging to the given userId,
     *         null if not found.
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public TaskModel getSingleTask(int userId, String taskNameSearch) throws SQLException{ 
   
        String sql = "SELECT * FROM genTaskTable WHERE userId = ? AND taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setString(2, taskNameSearch);

                try (ResultSet rs = ps.executeQuery()) {

                    // If no tasks were found rs will be empty and rs.next() will be false
                    if (rs.next()) {    
                        
                        // Creates a new TaskModel object for each task found and adds it to the list.
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

    /**
     * Gets the task ID for the given userId and task name.
     * 
     * @param userId the userId for the user whose task ID to get
     * @param taskName the name of the task
     * 
     * @return the task ID if the task exists for the given user, -1 otherwise
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public int getTaskId(int userId, String taskName) throws SQLException{

        String sql = "SELECT taskId FROM genTaskTable WHERE userId = ? AND taskName = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                
                ps.setInt(1, userId);
                ps.setString(2, taskName);

                try (ResultSet rs = ps.executeQuery()) {
                    
                    // If rs is not empty, the task was found and its id can be returned
                    if(rs.next()){
                        int taskID = rs.getInt(1);
                    
                        return taskID;   
                    }

                    return -1;                             
                }
            }
        }
    }
}
