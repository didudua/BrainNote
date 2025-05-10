package Lop48K14_1.group2.brainnote.ui.Tasks;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import Lop48K14_1.group2.brainnote.R;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("task_title");

        if (taskTitle == null) {
            Log.e("AlarmReceiver", "Task title is null");
            return;
        }

        Log.d("AlarmReceiver", "Task Reminder: " + taskTitle);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Kiểm tra quyền thông báo trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("AlarmReceiver", "Notification permission not granted");
                return;
            }
        }

        // Tạo kênh thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "task_notification_channel";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Task Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_notification_channel")
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle("Task Reminder")
                .setContentText("It's time to complete your task: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(taskTitle.hashCode(), builder.build());
    }
}
