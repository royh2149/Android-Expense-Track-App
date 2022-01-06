package com.example.expensetracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class InternetConnectionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        // Toast.makeText(context, "Phone Boot!", Toast.LENGTH_LONG).show();

        /* NotificationManager notif=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify=new Notification.Builder
                (context.getApplicationContext()).setContentTitle("BOOT UP").setContentText("body").
                setContentTitle("subject").setSmallIcon(R.drawable.ic_launcher_background).build();

        // notify.flags |= Notification.FLAG_AUTO_CANCEL;
        notif.notify(0, notify); */

        if (isOnline(context)){
            Toast.makeText(context, "INTERNET CONNECTION ONONON", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "INTERNET CONNECTION OFFOFFOFF", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline(Context context){
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager.getActiveNetworkInfo();
            return info != null && info.isConnected();
        } catch (Exception  e){
            e.printStackTrace();
            return false;
        }
    }
}