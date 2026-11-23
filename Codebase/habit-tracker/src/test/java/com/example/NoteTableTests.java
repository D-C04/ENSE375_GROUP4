package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


public class NoteTableTests {

    private String dbFile = "habitTrackerTestTemp.db";
    private DatabaseSetup dbSetup;
    private UserTable userTable;
    private NoteTable noteTable;
   

    @TempDir
    Path tempDir;

    // Creates a brand new temp database for each test
    @BeforeEach 
    void init() throws Exception {
        dbSetup = new DatabaseSetup(tempDir, dbFile);  
        dbSetup.createTables();
        userTable = new UserTable(dbSetup);
        noteTable = new NoteTable(dbSetup);
    }

    @Test
    void AddNote_FirstNote_OneRowInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        String query = "SELECT COUNT(*) FROM noteTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddNote_FirstNote_NoteInDbIsCorrect() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        String query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddNote_NoteAlreadyExistsForThatDay_OnlyOneRowInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());


        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel note2 = new NoteModel(userId, "This is maybe a note.", "12-12-2012");
        noteTable.addNote(note2);


        String query = "SELECT COUNT(*) FROM noteTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddNote_NoteAlreadyExistsForThatDay_DbIsUpdated() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        String query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        NoteModel note2 = new NoteModel(userId, "This is maybe a note.", "12-12-2012");
        noteTable.addNote(note2);

        query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(0, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is maybe a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddNote_DuplicateUserIdsDiffDay_TwoRowsInDb() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel note2 = new NoteModel(userId, "This is a note.", "12-13-2012");
        noteTable.addNote(note2);

        String query = "SELECT COUNT(*) FROM noteTable;";

        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(2, rs.getInt(1));
        }
    }

    @Test
    void AddNote_DuplicateIdsDiffDay_BothNotesInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel note2 = new NoteModel(userId, "This is a note.", "12-13-2012");
        noteTable.addNote(note2);

        String query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-13-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void AddNote_DupeDateDiffUserId_BothNotesInTable() throws SQLException{

        // If a user is not added the foreign key constraint will fail
        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        UserModel user2 = new UserModel("username2", "123456789", "gmail@gmail.com");
        userTable.addUser(user2);
        int userId2 = userTable.getUserId(user2.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel note2 = new NoteModel(userId2, "This is a note.", "12-12-2012");
        noteTable.addNote(note2);

        String query = "SELECT COUNT(*) FROM noteTable WHERE userId = 1 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }

        query = "SELECT COUNT(*) FROM noteTable WHERE userId = 2 AND noteText = 'This is a note.' AND noteDate = '12-12-2012';";
        try (Connection conn = dbSetup.openConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();) {
            rs.next();

            assertEquals(1, rs.getInt(1));
        }
    }



    @Test
    void GetSingleNote_NoteDoesNotExist_NullIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel userNote = noteTable.getSingleNote(userId, "12-12-2012");

        assertNull(userNote);

    }

    @Test
    void GetSingleNote_UserDoesNotExist_NullIsReturned() throws SQLException{

        NoteModel userNote = noteTable.getSingleNote(1, "12-12-2012");

        assertNull(userNote);

    }

    @Test
    void GetSingleNote_NoteExists_NullIsNotReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel userNote = noteTable.getSingleNote(userId, "12-12-2012");

        assertNotNull(userNote);

    }

    @Test
    void GetSingleNote_NoteExists_CorrectNoteObjectIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel userNote = noteTable.getSingleNote(userId, "12-12-2012");

        assertEquals(userTable.getUserId(user.getUsername()), userNote.getCorrespondingUserId());
        assertEquals("This is a note.", userNote.getNoteText());
        assertEquals("12-12-2012", userNote.getNoteDate());

    }

    @Test
    void GetSingleNote_NoteExistsMultNotes_CorrectNoteObjectIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel note2 = new NoteModel(userId, "This is a note.", "12-13-2012");
        noteTable.addNote(note2);

        NoteModel userNote = noteTable.getSingleNote(userId, "12-13-2012");

        assertEquals(userTable.getUserId(user.getUsername()), userNote.getCorrespondingUserId());
        assertEquals("This is a note.", userNote.getNoteText());
        assertEquals("12-13-2012", userNote.getNoteDate());

    }

    @Test
    void GetSingleNote_NoteForDayDoesNotExist_NullIsReturned() throws SQLException{

        UserModel user = new UserModel("username1", "123456789", "email@email.com");
        userTable.addUser(user);
        int userId = userTable.getUserId(user.getUsername());

        NoteModel note = new NoteModel(userId, "This is a note.", "12-12-2012");
        noteTable.addNote(note);

        NoteModel userNote = noteTable.getSingleNote(userId, "12-13-2012");

        assertNull(userNote);

    }

}
