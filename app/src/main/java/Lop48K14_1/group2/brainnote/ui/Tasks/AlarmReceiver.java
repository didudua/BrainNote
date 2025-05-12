package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import Lop48K14_1.group2.brainnote.R;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TaskReminderChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");
        if (taskTitle == null) {
            taskTitle = "Task Reminder";
        }

        // Create notification channel (required for Android 8.0+)
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Task Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show notification
        notificationManager.notify(taskTitle.hashCode(), builder.build());
    }
}