package com.example.expensetracker;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;

public class ActionSMSReceiver extends BroadcastReceiver {

    public static final String MESSAGE_MARKER = "ETAL"; // Expense Tracker App ALert
    public static final String MESSAGE_SUM_KEY = "sum";
    public static final String MESSAGE_DESC_KEY = "desc";
    public static final int KEY_INDEX = 0;
    public static final int VALUE_INDEX = 1;

    public static final String INTENT_SUM = "sum";
    public static final String INTENT_DESC = "sum";

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
    private final int NOTIFICATION_CANCEL_CODE = 0;
    private final String NOTIFICATION_CHANNEL_ID = "ETAL";

    NotificationManagerCompat notificationManager;
    NotificationChannel channel;
    NotificationManager manager;

    Context context;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            // ensure the broadcast message is a received SMS
        } else {
            return;
        }

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        notificationManager = NotificationManagerCompat.from(context);
        this.context = context;

        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus"); // SMS info

                for (int i = 0; i < pdusObj.length; i++) { // iterate through the messages
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress(); // extract sender phone number

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    long messageTimestamp = currentMessage.getTimestampMillis();

                    Log.d("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    // Show alert
                    // Toast.makeText(context, "senderNum: " + senderNum + ", message: " + message, Toast.LENGTH_LONG).show();
                    if (message.startsWith(MESSAGE_MARKER)){
                        Log.d("SmsReceiver", "message from myAPP!!!");
                        // Toast.makeText(context, "message from myAPP!!!", Toast.LENGTH_LONG).show();

                        String[] result = message.split("\n");
                        boolean isSum = false, isDesc = false;
                        int sum = 0;
                        String description = "";

                        for (String line : result){
                            String[] keyValue = line.split(":");
                            if (keyValue[KEY_INDEX].toLowerCase().contains(MESSAGE_SUM_KEY)){
                                sum = Integer.parseInt(keyValue[VALUE_INDEX].trim());
                                isSum = true;
                            } else if (keyValue[KEY_INDEX].toLowerCase().contains(MESSAGE_DESC_KEY)){
                                description = keyValue[VALUE_INDEX];
                                isDesc = true;
                            }
                        }

                        if (isSum && isDesc){
                            makeNotification(context.getString(R.string.newSMSReceived), context.getString(R.string.clickToAddAction), sum, description, messageTimestamp);
                        }
                    }
                } // end for loop
            } // bundle is null
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }

    private void makeNotification(String notiTitle, String notiText, int sum, String description, long messageTimestamp) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            createNotificationChannel();

            Intent go = new Intent(this.context, AddActionActivity.class);

            String extra = description + ":" + sum + ":" + messageTimestamp;
            go.putExtra(INTENT_DESC, extra);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this.context);
            stackBuilder.addNextIntentWithParentStack(go);

            // 3 - Get the PendingIntent containing the entire back stack
            PendingIntent pIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder notificationB = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(notiTitle)
                    .setContentText(notiText)
                    .setSmallIcon(R.drawable.icon)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            manager.notify(NOTIFICATION_CANCEL_CODE, notificationB.build());
        } else {
            // notifyBeforAPI26(notiTitle, notiText);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription(context.getString(R.string.app_name));
            channel.setLightColor(Color.GREEN);

            manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}