package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * NoteTable - Holds methods used to access the note database table.
 * All methods in this class use prepared statements to prevent SQL injection.
 * Methods open and close their own database connections.
 * 
 * noteTable columns in order: noteId (primary key, auto-increment), -- Do not insert values into this one
 *                             userId (foreign key, references userTable.userId),
 *                             noteText (text, not null),
 *                             noteDate (text, unique with userId, not null)
 */
public class NoteTable {

    private DatabaseSetup db;

    public NoteTable(DatabaseSetup db){
        this.db = db;
    }

    /**
     * Adds the info in the given note model to the database.
     * If the userId and noteDate already exist in the database, 
     * the new note will replace the old note.
     * 
     * @param note the note model to add to the database
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public void addNote(NoteModel note) throws SQLException{

        // The prepared statement is used to prevent SQL injection.
        // The ? are placeholders for the values that will be put into the database.
        String sql = "INSERT OR REPLACE INTO noteTable (userId, noteText, noteDate) VALUES (?, ?, ?)" ;
        try(Connection conn = db.openConnection()){

            try(PreparedStatement ps = conn.prepareStatement(sql)){

                // Sets the values for the placeholders in the prepared statement
                ps.setInt(1, note.getCorrespondingUserId()); // Sets the userId as the first ? in the sql string
                ps.setString(2, note.getNoteText());
                ps.setString(3, note.getNoteDate());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Gets a single note from the database for the given userId and noteDate.
     * 
     * @param userId the userId to search for
     * @param searchNoteDate the noteDate to search for
     * 
     * @return a NoteModel object filled with the note's information,
     *         null if no note was found
     * 
     * @throws SQLException if the database cannot be accessed
     */
    public NoteModel getSingleNote(int userId, String searchNoteDate) throws SQLException{ 
   
        String sql = "SELECT * FROM noteTable WHERE userId = ? AND noteDate = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){

                ps.setInt(1, userId);
                ps.setString(2, searchNoteDate);

                try (ResultSet rs = ps.executeQuery()) {

                    // If no notes were found rs will be empty and rs.next() will be false
                    if (rs.next()) {       
                        
                        // Creates a new NoteModel object for the note found and returns it.
                        int correspondingUserId = rs.getInt("userId");
                        String noteText = rs.getString("noteText");
                        String noteDate = rs.getString("noteDate");

                        NoteModel note = new NoteModel(correspondingUserId, noteText, noteDate);
                        
                        return note;
                    }
                    else{
                        return null;
                    }
                    
                }                          
            }
        }
    }

    
}
