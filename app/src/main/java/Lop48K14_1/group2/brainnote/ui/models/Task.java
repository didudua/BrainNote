package Lop48K14_1.group2.brainnote.ui.models;

import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private boolean isCompleted;
    private boolean isFlagged;
    private int priority; // 0: Low, 1: Medium, 2: High
    
    public Task() {
        // Required empty constructor for Firebase
        this.id = UUID.randomUUID().toString();
    }
    
    public Task(String title, String description, String dueDate, boolean isFlagged, int priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = false;
        this.isFlagged = isFlagged;
        this.priority = priority;
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDueDate() {
        return dueDate;
    }
    public String getReminder() {
        return dueTime;
    }
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    public void setReminder(String dueDate) {
        this.dueTime = dueDate;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    
    public boolean isFlagged() {
        return isFlagged;
    }
    
    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
