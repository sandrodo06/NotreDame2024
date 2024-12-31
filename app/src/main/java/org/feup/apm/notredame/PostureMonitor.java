package org.feup.apm.notredame;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



import java.util.LinkedList;
import java.util.Queue;

public class PostureMonitor {

    private static final int MAX_BLOCKS = 60; // number of time blocks to check before sending an alert
    private Queue<Boolean> postureHistory;
    private static final String CHANNEL_ID = "bad_posture_channel";
    private static final int NOTIFICATION_ID = 1;
    private Context context;
    private boolean notificationSent;
    private boolean repeatAlertsEnabled;
    private SharedPreferences sharedPreferences;
    private static final String PREFERENCE_NAME = "app_preferences";
    private static final String SILENT_NOTIFICATIONS_KEY = "silent_notifications_enabled";
    private static final String REPEAT_ALERTS_KEY = "repeat_alerts_enabled";
    private int additionalBadPostureCount;
    private static final int NOTIFICATION_INTERVAL = 60; // number of time blocks to check before repeating an alert
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public PostureMonitor(AppCompatActivity activity) {
        this.context = activity;
        this.postureHistory = new LinkedList<>();
        this.notificationSent = false;
        this.additionalBadPostureCount = 0;

        // get the notifications configuration
        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        this.repeatAlertsEnabled = sharedPreferences.getBoolean(REPEAT_ALERTS_KEY, false);


        // Initialize posture history with default false states adding placeholder data
        for (int i = 0; i < MAX_BLOCKS; i++) {
            postureHistory.add(false);
        }

        // Permission request for notifications
        requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // If notifications disabled request permission (Android Tiramisu and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                requestNotificationPermission();
            }
        }
    }

    // Request notification permission method
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS); // Request specific notification permission
        }
    }

    // Add new posture data to posture history
    public void addPostureData(boolean isBadPosture) {
        if (postureHistory.size() >= MAX_BLOCKS) {
            postureHistory.poll();
        }
        postureHistory.offer(isBadPosture);

        //Send notification
        if (isBadPosture) {
            if (allBadPosture()) {
                if (!notificationSent) {
                    sendBadPostureNotification();
                    notificationSent = true;
                } else if (repeatAlertsEnabled) {
                    additionalBadPostureCount++;


                    if (additionalBadPostureCount >= NOTIFICATION_INTERVAL) {
                        sendBadPostureNotification();
                        additionalBadPostureCount = 0;
                    }
                }
            }
        } else {
            notificationSent = false;
            additionalBadPostureCount = 0;
        }
    }


    // Check if all blocks are true meaning they are all bad posture
    private boolean allBadPosture() {
        for (boolean isBadPosture : postureHistory) {
            if (!isBadPosture) {
                return false;
            }
        }
        return true;
    }

    // Send notification method
    private void sendBadPostureNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Toast.makeText(context, "Notifications are disabled. Please enable them.", Toast.LENGTH_LONG).show();
            return;
        }
        createNotification();
    }

    // Create notification and recreate notification methods with failed attempt at creating silent notifications
    private void createNotification() {
        boolean isSilent = sharedPreferences.getBoolean(SILENT_NOTIFICATIONS_KEY, false);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;


        recreateNotificationChannel(notificationManager, isSilent);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Bad Posture Detected")
                .setContentText("Please adjust your posture.")
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setAutoCancel(true);


        if (!isSilent) {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        } else {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        }

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void recreateNotificationChannel(NotificationManager notificationManager, boolean isSilent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = isSilent ? NotificationManager.IMPORTANCE_LOW : NotificationManager.IMPORTANCE_HIGH;

            Log.d("NotificationChannel", "Recreating channel with silent = " + isSilent);

            NotificationChannel existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

            if (existingChannel != null) {
                Log.d("NotificationChannel", "Deleting existing channel...");
                notificationManager.deleteNotificationChannel(CHANNEL_ID); // Delete the existing channel
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e("NotificationChannel", "Sleep interrupted while recreating notification channel");
            }


            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Posture Alerts",
                    importance
            );
            channel.setDescription("Notifications for bad posture alerts");

            if (isSilent) {
                Log.d("NotificationChannel", "Setting silent mode");
                channel.setSound(null, null);
                channel.enableVibration(false);
                channel.enableLights(false);
            } else {
                Log.d("NotificationChannel", "Setting normal mode");
                channel.setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION), Notification.AUDIO_ATTRIBUTES_DEFAULT);
                channel.enableVibration(true);
                channel.enableLights(true);
            }

            notificationManager.createNotificationChannel(channel);
        }
    }


}