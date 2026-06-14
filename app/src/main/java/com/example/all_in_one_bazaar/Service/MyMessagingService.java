package com.example.all_in_one_bazaar.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;

import com.example.all_in_one_bazaar.ui.client.home.MainActivity;
import com.example.all_in_one_bazaar.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyMessagingService extends FirebaseMessagingService {

    // ⚠️ Changed channel ID — forces Android to recreate channel with IMPORTANCE_HIGH
    // (Android ignores updates to existing channels, so new ID = fresh heads-up channel)
    private static final String CHANNEL_ID = "bazaar_headsup_v2";
    private static final String CHANNEL_NAME = "Bazaar Notifications";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String message = null;

        // 1. Notification payload (Firebase Console)
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        // 2. Data payload (custom API / Admin app)
        if (remoteMessage.getData().size() > 0) {
            if (title == null || title.isEmpty()) {
                title = remoteMessage.getData().get("title");
            }
            if (message == null || message.isEmpty()) {
                message = remoteMessage.getData().get("message");
            }
        }

        // Null-safe fallbacks
        if (title == null || title.isEmpty()) title = "All In One Bazaar";
        if (message == null || message.isEmpty()) message = "You have a new notification.";

        showHeadsUpNotification(title, message);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid())
                    .child("fcmToken")
                    .setValue(token);
        }
    }

    private void showHeadsUpNotification(String title, String message) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Default system notification sound URI
        Uri soundUri = Settings.System.DEFAULT_NOTIFICATION_URI;

        // Android 8.0+ — Create channel with IMPORTANCE_HIGH for heads-up popup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH  // ← This is what triggers heads-up popup
            );
            channel.setDescription("Order updates, offers and alerts");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setShowBadge(true);

            // Attach sound to the channel (required for heads-up on some devices)
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            manager.createNotificationChannel(channel);
        }

        // Intent when user taps the notification
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("from_notification", true);

        int notifId = (int) System.currentTimeMillis();

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // setFullScreenIntent — forces heads-up popup even on locked screen
        PendingIntent fullScreenIntent = PendingIntent.getActivity(
                this,
                notifId + 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)   // ← MAX for guaranteed heads-up
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen too
                .setSound(soundUri)
                .setVibrate(new long[]{0, 300, 200, 300})
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setContentIntent(contentIntent)
                .setFullScreenIntent(fullScreenIntent, true); // ← Forces popup banner

        manager.notify(notifId, builder.build());
    }
}