package com.example.expensetracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MonthlyGoalsService extends Service {
    NotificationManager manager;
    NotificationChannel channel;
    final String NOTIFICATION_CHANNEL_ID= "TIME_NOTI" ;
    final static int REPORT_DAY = 1;

    public MonthlyGoalsService() {
    }

    String serviceName = "MonthlyGoalsService";
    String username, notificationText = "";
    double totalIncome = 0.0, totalOutcome = 0.0, monthlyBalance = 0.0;
    ArrayList<Action> pastMonthActions;
    LocalDateTime today;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(NOTIFICATION_CHANNEL_ID, "Service onCreate : " + serviceName);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(NOTIFICATION_CHANNEL_ID, "Service onStartCommand : " + serviceName);

        // get the current date, reset time to beginning of day
        today = LocalDateTime.now();
        today.minusHours(today.getHour());
        today.minusMinutes(today.getMinute());
        today.minusSeconds(today.getSecond());

        // report once a month, on a specific day
        if (today.getDayOfMonth() != REPORT_DAY){
            Toast.makeText(this, "bad day", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        LocalDateTime startDate = HelperMethods.getMonthBefore(today); // calculate start date

        determineUsername(); // determine who is the last logged-in username

        // calculate total income, outcome and balance
        pastMonthActions = DatabaseTools.getActionsOfUserSinceUntil(username, startDate, today);
        determineMonthlyValues();

        // create notification body
        generateNotificationText();

        makeNotification(getString(R.string.app_name) + " " + getString(R.string.monthlyReport), notificationText, this);
        return super.onStartCommand(intent, flags, startId);
    }

    private void makeNotification(String notiTitle, String notiText, Context context) {
        System.out.println("" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(context.getString(R.string.app_name));
            channel.setLightColor(Color.GREEN);

            //1 - create intent (will be called upon pressing the notification)
            Intent go = new Intent(this, MonthlyGoalsActivity.class);
            go.putExtra("kissCount", 3);

            //2 -  Create the TaskStackBuilder and add the intent, which inflates the back stack
            //     this is done to enable navigate using back
            // DONT FORGET TO ADD TO MANIFEST!!!

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addNextIntentWithParentStack(go);

            // 3 - Get the PendingIntent containing the entire back stack
            PendingIntent pIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            Notification.Builder notificationB = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(notiTitle)
                    //.setContentText(notiText)
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pIntent)// move to another intent
                    //  .addAction(R.drawable.my_noti_icon, "Do something", pendingIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(notiText))
                    .setAutoCancel(true);

            manager.notify(0, notificationB.build());
        } else {
//            notifyBeforAPI26(notiTitle, notiText);
        }
    }

    private void determineUsername(){
        SharedPreferences sp = getSharedPreferences(MainActivity.generalSettingsSpName, MODE_PRIVATE);
        username = sp.getString(MainActivity.generalSettingsSpCurrentUsername, "idanl");
    }

    private void determineMonthlyValues(){
        for (Action action : pastMonthActions){ // traverse through all the actions
            if (action instanceof Income){
                // if the current action is an income, update the income sum and increase the balance
                totalIncome += action.getSum();
                monthlyBalance += action.getSum();
            } else {
                // if the current action is an outcome, update the outcome sum and reduce the balance
                totalOutcome += action.getSum();
                monthlyBalance -= action.getSum();
            }
        }
    }

    private void generateNotificationText(){
        notificationText += getString(R.string.monthlyReport) + " - " + HelperMethods.dateAsString(today) + "\n";
        notificationText += getString(R.string.yourIncome) + " " + totalIncome + "\n";
        notificationText += getString(R.string.yourOutcome) + " " +totalOutcome + "\n";
        notificationText += getString(R.string.monthlyBalance) + " " + monthlyBalance;
    }

    @Override
    public void onDestroy() {
        Log.d(NOTIFICATION_CHANNEL_ID, "Service onDestroy : " + serviceName);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}