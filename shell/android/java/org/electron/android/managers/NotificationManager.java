// Copyright 2024 Electron Android Project
// Notification Manager for Electron Android

package org.electron.android.managers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.electron.android.ElectronActivity;
import org.electron.android.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages notifications for Electron Android.
 */
public class NotificationManager {

    private static final String CHANNEL_ID = "electron_notifications";
    private static final String CHANNEL_NAME = "Notifications";
    
    private Context context;
    private android.app.NotificationManager systemManager;
    private Map<String, Integer> notificationIds;

    public NotificationManager(Context context) {
        this.context = context;
        this.notificationIds = new HashMap<>();
        this.systemManager = (android.app.NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Electron application notifications");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            systemManager.createNotificationChannel(channel);
        }
    }

    /**
     * Show a notification.
     * @return notification ID
     */
    public String showNotification(String title, String body, String icon) {
        String notificationId = UUID.randomUUID().toString();
        int id = notificationId.hashCode();
        notificationIds.put(notificationId, id);

        Intent intent = new Intent(context, ElectronActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());

        return notificationId;
    }

    /**
     * Cancel a notification.
     */
    public void cancelNotification(String notificationId) {
        Integer id = notificationIds.get(notificationId);
        if (id != null) {
            NotificationManagerCompat.from(context).cancel(id);
            notificationIds.remove(notificationId);
        }
    }

    /**
     * Cancel all notifications.
     */
    public void cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll();
        notificationIds.clear();
    }
}
