package com.example.expensetracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import java.time.LocalDateTime;

public class SendNotificationReceiver extends BroadcastReceiver {

    NotificationManager manager;
    NotificationChannel channel;
    final String NOTIFICATION_CHANNEL_ID="TIME_NOTI";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        makeNotification("noti title", "notification after timeout",context);
        Toast.makeText(context, "here", Toast.LENGTH_LONG).show();
    }

    private void makeNotification(String notiTitle, String notiText, Context context) {
        if (LocalDateTime.now().getDayOfMonth() % 2 == 0){
            return;
        }

        System.out.println("" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(context.getString(R.string.app_name));
            channel.setLightColor(Color.GREEN);

            manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Notification.Builder notificationB = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(notiTitle)
                    .setContentText(notiText)
                    .setSmallIcon(R.drawable.icon)
                    .setAutoCancel(true);

            manager.notify(0, notificationB.build());
        } else {
//            notifyBeforAPI26(notiTitle, notiText);
        }
    }
}