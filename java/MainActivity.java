package com.example.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String generalSettingsSpName = "general_settings";
    public static final String generalSettingsSpCurrentUsername = "current_username";

    InternetConnectionReceiver receiver;

    SharedPreferences generalSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalSettings = getSharedPreferences(generalSettingsSpName, MODE_PRIVATE); // open the general settings sharedPreference

        // allow networking actions in main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        receiver = new InternetConnectionReceiver();
        doAlarm();
    }

    public void launchSignUp(View view){

        //startActivity(new Intent(this, SignUpActivity.class));
        startActivity(new Intent(this, MonthlyGoalsActivity.class));
    }

    public void launchSignIn(View view){
        startActivity(new Intent(this, SignInActivity.class));
    }

    private void doAlarm() {
        Calendar c = Calendar.getInstance();
        // c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR, 9);
        c.set(Calendar.MINUTE, 30);
        // c.set(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(MainActivity.this, MonthlyGoalsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this,
                0, intent, 0);

        /* Intent intent = new Intent(MainActivity.this, SendNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                1, intent, 0); */

        //     Toast.makeText(this,c.getTimeInMillis()+" "+c.getTime(),Toast.LENGTH_LONG).show();

        AlarmManager alma = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // alma.cancel(pendingIntent); // cancel every existing alarms
        alma.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}