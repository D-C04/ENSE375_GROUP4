package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NoteTable {

    private DatabaseSetup db;

    public NoteTable(DatabaseSetup db){
        this.db = db;
    }

    public void addNote(NoteModel note) throws SQLException{
        String sql = "INSERT OR REPLACE INTO noteTable (userId, noteText, noteDate) VALUES (?, ?, ?)" ;
        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, note.getCorrespondingUserId());
                ps.setString(2, note.getNoteText());
                ps.setString(3, note.getNoteDate());
                ps.executeUpdate();
            }
        }
    }

    public NoteModel getSingleNote(int userId, String searchNoteDate) throws SQLException{ 
   
        String sql = "SELECT * FROM noteTable WHERE userId = ? AND noteDate = ?";

        try(Connection conn = db.openConnection()){
            try(PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, userId);
                ps.setString(2, searchNoteDate);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {             
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
