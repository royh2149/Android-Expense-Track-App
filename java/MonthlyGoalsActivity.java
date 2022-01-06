package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class MonthlyGoalsActivity extends AppCompatActivity {

    SharedPreferences storage;

    ArrayList<Action> monthlyActions;
    String[] categoriesArr;
    double[] sumsArr;

    LinearLayout llGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_goals);

        // TODO: change the testing username to one received from intent, update MAINACTOVOTYY

        // get the actions of the user during a specific date range
        monthlyActions = DatabaseTools.getActionsOfUserSinceUntil("idanl", LocalDateTime.of(2021, 11, 1, 0, 0), LocalDateTime.of(2021, 12, 1, 0, 0));
        System.out.println(monthlyActions);

        // instantiate the arrays
        categoriesArr = getResources().getStringArray(R.array.categories);
        sumsArr = new double[categoriesArr.length];

        storage = getSharedPreferences(HelperMethods.getSpFilename("idanl"), MODE_PRIVATE); // access to shared preference
        // storage.getString()

        HelperMethods.fillDataArrays(categoriesArr, sumsArr, monthlyActions);

        llGoals = findViewById(R.id.llGoals);

        checkGoalsReached();
        System.out.println("HERE");
    }

    private void checkGoalsReached(){
        for (int i = 0; i < categoriesArr.length; i++){ // traverse all the categories
            String category = categoriesArr[i];
            int limit = storage.getInt(category, 0);

            TextView tv = new TextView(this);
            tv.setTextColor(Color.BLUE);
            tv.setTextSize(30.0f);

            if (!category.equals(getString(R.string.salary))){
                if (limit < sumsArr[i]){
                    tv.setText(category + " You Have Exceeded Your LIMIT!");
                } else if (limit == sumsArr[i]){
                    tv.setText(category + " You Have REACHED Your LIMIT!");
                } else {
                    tv.setText(category + " You Have not Exceeded Your LIMIT! Well Done!");
                }
            } else {
                if (limit > sumsArr[i]){
                    tv.setText(category + " You Have not reached Your GOAL!");
                } else if (limit == sumsArr[i]){
                    tv.setText(category + " You Have REACHED Your GOAL!");
                } else {
                    tv.setText(category + " You Have Exceeded Your GOAL! Well Done!");
                }
            }

            llGoals.addView(tv);
        }
    }


}