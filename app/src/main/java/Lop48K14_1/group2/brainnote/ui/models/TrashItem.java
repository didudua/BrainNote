package Lop48K14_1.group2.brainnote.ui.models;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class TrashItem {
    private Notebook notebook;  // Lưu sổ tay đã xóa
    private Note note;          // Lưu ghi chú đã xóa
    private String originalNotebookId; // ID của sổ tay gốc
    private long deletedAt;     // Thời gian xóa

    // Constructor cho TrashItem
    public TrashItem() {
        // Default constructor
    }

    // Getter và Setter cho sổ tay
    public Notebook getNotebook() {
        return notebook;
    }

    public void setNotebook(Notebook notebook) {
        this.notebook = notebook;
    }

    // Getter và Setter cho ghi chú
    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    // Getter và Setter cho ID sổ tay gốc
    public String getOriginalNotebookId() {
        return originalNotebookId;
    }

    public void setOriginalNotebookId(String originalNotebookId) {
        this.originalNotebookId = originalNotebookId;
    }

    // Getter và Setter cho thời gian xóa
    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }
}