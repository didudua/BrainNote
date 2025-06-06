package Lop48K14_1.group2.brainnote.ui.models;

public class Note {
    private String id;
    private String title;
    private String content;
    private String date;
    private String notebookId;

    public Note(String id, String noteBookId, String title, String content, String date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.notebookId = noteBookId;
    }

    public Note() {
        // Required by Firebase
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }
}
