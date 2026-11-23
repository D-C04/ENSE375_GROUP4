package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    
}
