package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class AnalyticsActivity extends AppCompatActivity {

    TextView tvAnalytics;

    String analysis;

    ArrayList<Action> actions;
    ArrayList<Income> incomes;
    ArrayList<Outcome> outcomes;
    Double[] outcomeSums, incomeSums;
    Double totalIncome, totalOutcome;
    String[] categoriesArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        tvAnalytics = findViewById(R.id.tvAnalytics);
        tvAnalytics.setMovementMethod(new ScrollingMovementMethod());



        // read the various categories of the actions
        categoriesArr = getResources().getStringArray(R.array.categories);

        // init arrays and lists
        init_data();

        analysis = analyze();
        tvAnalytics.setText(analysis);
    }

    private void init_data(){
        System.out.println(categoriesArr.length);
        totalIncome = 0.0;
        totalOutcome = 0.0;
        outcomeSums = new Double[categoriesArr.length];
        incomeSums = new Double[categoriesArr.length];

        // initialize both arrays to zero
        for (int i = 0; i < categoriesArr.length; i++){
            outcomeSums[i] = 0.0;
            incomeSums[i] = 0.0;
        }

        // initialize lists, retrieve the actions of the user from the DB
        actions = DatabaseTools.getActionsOfUser(getIntent().getExtras().getString(DashboardActivity.USERNAME_INTENT));
        incomes = new ArrayList<>();
        outcomes = new ArrayList<>();
    }

    private String analyze(){
        String result = "";
        String[] advices = getResources().getStringArray(R.array.advices);

        setupData(); // generate the necessary data, on which the analysis will be based

        if (totalOutcome > totalIncome){
            result += getResources().getString(R.string.moreOutcome) + "\n";
        } else if (totalIncome > totalOutcome){
            result += getResources().getString(R.string.moreIncome) + "\n";
        } else {
            result += getResources().getString(R.string.equalInOut) + "\n";
        }

        // get the most significant segments of the user's actions
        int[] incomeMaxes = findTwoMax(incomeSums);
        int[] outcomeMaxes = findTwoMax(outcomeSums);

        // express main income and outcome sources
        result += "Your income comes mostly from " + categoriesArr[incomeMaxes[0]];
        if (incomeMaxes[0] != incomeMaxes[1]){ // type 2 sources only in case those exist
            result += " and " + categoriesArr[incomeMaxes[1]] + ".";
        }

        result += "\n"; // separating line
        result += "Your outcome comes mostly from " + categoriesArr[outcomeMaxes[0]];

        if (outcomeMaxes[0] != outcomeMaxes[1]){ // type 2 sources only in case those exist
            result += " and " + categoriesArr[outcomeMaxes[1]] + ".\n";
            result += advices[outcomeMaxes[1]]; // add the advice of the second max outcome source, if exists
        }

        result += "\n"; // separating line
        result += advices[outcomeMaxes[0]] + "\n"; // add the advice of the first max outcome source
        result += advices[incomeMaxes[0]] + "\n"; // add income advice


        // TODO: implement analysis algorithm

        return result;
    }

    private void setupData(){
        // generate the adapter between the categories & sums array
        HashMap<String, Integer> indexMap = HelperMethods.generateIndexMap(categoriesArr);

        for (Action action : actions){
            if (action instanceof Income){
                incomes.add((Income)action);
                int index = indexMap.get(action.getCategory());
                System.out.println(incomeSums[index]);
                incomeSums[index] += action.getSum();
                totalIncome += action.getSum();
            } else if (action instanceof  Outcome){
                outcomes.add((Outcome)action);
                int index = indexMap.get(action.getCategory());
                outcomeSums[index] += action.getSum();
                totalOutcome += action.getSum();
            }
        }
    }

    private int[] findTwoMax(Double[] target){
        int[] result = {0, 1};

        if (target[0] < target[1]){
            result[0] = 1;
            result[1] = 0;
        }

        for (int i = 0; i < target.length; i++){
            if (target[i] > target[result[1]]){ // compare the current item to the second-max
                if (target[i] > target[result[0]]){ // compare it to the max
                    result[1] = result[0];
                    result[0] = i;
                } else {
                    result[1] = i;
                }
            }
        }

        return result;
    }
}