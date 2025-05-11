package Lop48K14_1.group2.brainnote.ui.models;

public class Notification {
    private String title;
    private String content;
    private long timestamp;

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(NotificationModel.class)
    }

    public Notification(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}