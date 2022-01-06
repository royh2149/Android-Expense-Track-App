package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    public static final String USER_PASS_FRAGMENT = "UserPassFragment";
    public static final String CSV_EXPORT_FRAGMENT = "CSVExportFragment";
    public static final String MONTHLY_STATS_FRAGMENT = "MonthlyStatsFragment";

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    UserPassFragment userPassFragment;
    CSVExportFragment csvExportFragment;
    MonthlyStatsFragment monthlyStatsFragment;

    Fragment curSetting;
    Bundle usernameBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction(); // to load the fragment

        usernameBundle = new Bundle();
        usernameBundle.putString(UserPassFragment.ARG_PARAM1, getIntent().getExtras().getString(DashboardActivity.USERNAME_INTENT));

        userPassFragment = new UserPassFragment();
        csvExportFragment = new CSVExportFragment();
        monthlyStatsFragment = new MonthlyStatsFragment();

        // add the arguments
        userPassFragment.setArguments(usernameBundle);
        csvExportFragment.setArguments(usernameBundle);
        monthlyStatsFragment.setArguments(usernameBundle);

        fragmentTransaction.add(R.id.llCurSetting, csvExportFragment, CSV_EXPORT_FRAGMENT);
        fragmentTransaction.commit();

        //curSetting = (Fragment) findViewById(R.id.curSetting);
    }

    public void launchUserPassFragment(View view){
        UserPassFragment fragment1 = (UserPassFragment) fragmentManager.findFragmentByTag(USER_PASS_FRAGMENT);
        if (fragment1 == null){
            fragment1 = new UserPassFragment();
            fragment1.setArguments(usernameBundle);
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.llCurSetting, fragment1, USER_PASS_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void launchCSVExportFragment(View view){
        CSVExportFragment fragment1 = (CSVExportFragment) fragmentManager.findFragmentByTag(CSV_EXPORT_FRAGMENT);
        if (fragment1 == null){
            fragment1 = new CSVExportFragment();
            fragment1.setArguments(usernameBundle);
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.llCurSetting, fragment1, CSV_EXPORT_FRAGMENT);
        fragmentTransaction.commit();
    }

    public void launchMonthlyStatsFragment(View view){
        MonthlyStatsFragment fragment1 = (MonthlyStatsFragment) fragmentManager.findFragmentByTag(MONTHLY_STATS_FRAGMENT);
        if (fragment1 == null){
            fragment1 = new MonthlyStatsFragment();
            fragment1.setArguments(usernameBundle);
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.llCurSetting, fragment1, MONTHLY_STATS_FRAGMENT);
        fragmentTransaction.commit();
    }
}