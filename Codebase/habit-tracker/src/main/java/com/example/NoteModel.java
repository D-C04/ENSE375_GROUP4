package com.example;

/**
 * NoteModel - Holds a note's information so it can be passed to and from the database classes.
 * Holds: correspondingUserId, noteText, noteDate.
 * Immutable: values are set in the constructor and can only be read with getters.
 */
public class NoteModel {

    private int correspondingUserId;
    private String noteText;
    private String noteDate; 

    public NoteModel(int correspondingUserId, String noteText, String noteDate){
        this.correspondingUserId = correspondingUserId;
        this.noteText = noteText;
        this.noteDate = noteDate;
    }

    public int getCorrespondingUserId(){
        return correspondingUserId;
    }

    public String getNoteText(){
        return noteText;
    }

    public String getNoteDate(){
        return noteDate;
    }

}
