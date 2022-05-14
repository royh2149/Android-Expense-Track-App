package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class MonthlyGoalsActivity extends AppCompatActivity {

    SharedPreferences storage;
    String username;

    ArrayList<Action> monthlyActions;
    String[] categoriesArr;
    double[] sumsArr;

    // LinearLayout llGoals;
    ListView lvGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_goals);

        username = getIntent().getExtras().getString(DashboardActivity.USERNAME_INTENT);

        // get the actions of the user during a specific date range
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = HelperMethods.getMonthBefore(today); // calculate start date
        monthlyActions = DatabaseTools.getActionsOfUserSinceUntil(username, startDate, today);

        // instantiate the arrays
        categoriesArr = getResources().getStringArray(R.array.categories);
        sumsArr = new double[categoriesArr.length];

        storage = getSharedPreferences(HelperMethods.getSpFilename(username), MODE_PRIVATE); // access to shared preference
        // storage.getString()

        HelperMethods.fillDataArrays(categoriesArr, sumsArr, monthlyActions);

        // llGoals = findViewById(R.id.llGoals);
        lvGoals = findViewById(R.id.lvGoals);

        checkGoalsReached();
    }

    private void checkGoalsReached(){
        ArrayList<String> goals = new ArrayList<>();
        for (int i = 0; i < categoriesArr.length; i++){ // traverse all the categories
            String category = categoriesArr[i];
            int limit = storage.getInt(category, -1);

            if (limit < 0){ // if no goal set, skip the current category
                continue;
            }

            if (!category.equals(getString(R.string.salary))){
                if (limit < sumsArr[i]){
                    goals.add("You Have Exceeded Your LIMIT!");
                } else if (limit == sumsArr[i]){
                    goals.add("You Have REACHED Your LIMIT!");
                } else {
                    goals.add("You Have not Exceeded Your LIMIT! Well Done!");
                }
            } else {
                if (limit > sumsArr[i]){
                    goals.add("You Have not reached Your GOAL!");
                } else if (limit == sumsArr[i]){
                    goals.add("You Have REACHED Your GOAL!");
                } else {
                    goals.add("You Have Exceeded Your GOAL! Well Done!");
                }
            }

            //llGoals.addView(tv);
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_2, android.R.id.text1, goals) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tvCategory = (TextView) view.findViewById(android.R.id.text1);
                    TextView tvResult = (TextView) view.findViewById(android.R.id.text2);

                    tvCategory.setTextSize(20);

                    tvCategory.setText(categoriesArr[position]);
                    tvResult.setText(goals.get(position));
                    return view;
                }
            };
            lvGoals.setAdapter(adapter);
        }
    }


}