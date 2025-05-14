package Lop48K14_1.group2.brainnote.ui.models;

import java.util.ArrayList;
import java.util.List;

public class Notebook {
    private String id;
    private String name;
    private List<Note> notes;
    public Notebook() {
        // cần thiết cho Firebase
    }

    public Notebook(String id, String name, List<Note> notes) {
        this.id = id;
        this.name = name;
        this.notes = notes != null ? notes : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
    public Note getNoteById(String noteId) {
        for (Note note : notes) {
            if (note.getId().equals(noteId)) {
                return note;
            }
        }
        return null;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void addNote(Note note) {
        notes.add(note);
    }
}
