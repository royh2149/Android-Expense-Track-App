package com.example.expensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String generalSettingsSpName = "general_settings";
    public static final String generalSettingsSpCurrentUsername = "current_username";

    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;

    InternetConnectionReceiver receiver;

    SharedPreferences generalSettings;

    ImageView ivMainLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalSettings = getSharedPreferences(generalSettingsSpName, MODE_PRIVATE); // open the general settings sharedPreference

        // allow networking actions in main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ivMainLogo = findViewById(R.id.ivMainLogo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.enlarge);
        ivMainLogo.startAnimation(animation);

        // receiver = new InternetConnectionReceiver();
        doAlarm();

        receiveSMSPermission();

        BroadcastReceiver br = new ActionSMSReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(br, filter);
    }

    public void launchSignUp(View view){
        startActivity(new Intent(this, SignUpActivity.class));
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
        PendingIntent pendingIntent = PendingIntent.getService(MainActivity.this, 0, intent, 0);

        AlarmManager alma = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alma.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void receiveSMSPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_RECEIVE_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(receiver);
    }
}