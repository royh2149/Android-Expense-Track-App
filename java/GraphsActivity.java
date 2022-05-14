package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GraphsActivity extends AppCompatActivity {

    private static final String TAG = "GRAPH_TAG";
    private static Class CLASS_TO_DISPLAY = Outcome.class;

    PieChart pcMain;
    Spinner timeSpansGraphsSpinner, actionTypeGraphsSpinner;

    ArrayList<Action> actions;
    ArrayList<Action> toView;

    ArrayAdapter<CharSequence> adapter, secondAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphs);

        setupSpinners();

        pcMain = findViewById(R.id.pcMain);
        Description desc = new Description();
        desc.setText("Outcomes by Category");
        desc.setTextSize(15f);

        pcMain.setDescription(desc);
        pcMain.setRotationEnabled(true);
        pcMain.setUsePercentValues(true);
        //pieChart.setHoleColor(Color.BLUE);
        //pieChart.setCenterTextColor(Color.BLACK);
        pcMain.setHoleRadius(25f);
        pcMain.setTransparentCircleAlpha(0);
        pcMain.setCenterText("Your Actions");
        pcMain.setCenterTextSize(10);
        //pieChart.setDrawEntryLabels(true);
        //pieChart.setEntryLabelTextSize(20);
        //More options just check out the documentation!

        actions = DatabaseTools.getActionsOfUser(getIntent().getExtras().getString(DashboardActivity.USERNAME_INTENT));

        setupToView(CLASS_TO_DISPLAY, getResources().getString(R.string.allTime));
        addDataSet();
    }

    private void setupSpinners(){
        timeSpansGraphsSpinner = findViewById(R.id.time_spans_graphs_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this, R.array.timeSpans, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        timeSpansGraphsSpinner.setAdapter(adapter);

        // the same for the action type spinner
        actionTypeGraphsSpinner = findViewById(R.id.action_type_graphs_spinner);
        secondAdapter = ArrayAdapter.createFromResource(this, R.array.actions, android.R.layout.simple_spinner_item);
        secondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionTypeGraphsSpinner.setAdapter(secondAdapter);


        // add the listener to update the graph when necessary
        SpinnerListener spinnerListener = new SpinnerListener();
        timeSpansGraphsSpinner.setOnItemSelectedListener(spinnerListener);
        actionTypeGraphsSpinner.setOnItemSelectedListener(spinnerListener);

        // set defaults
        timeSpansGraphsSpinner.setSelection(4); // default action: outcome
        actionTypeGraphsSpinner.setSelection(1); // default time span: all time
    }

    private void setupToView(Class cls, String dateTime){
        toView = new ArrayList<>();
        for (Action action : actions){
            if (cls.isInstance(action)){
                if (action.getDate().isAfter(HelperMethods.getStartDate(dateTime, this))){
                    toView.add(action);
                }
            }
        }
    }

    private void addDataSet() {
        Log.d(TAG, "addDataSet started");
        ArrayList<PieEntry> yEntrys = new ArrayList<>();

        String[] categoriesArr = getResources().getStringArray(R.array.categories);
        double[] sumsArr = new double[categoriesArr.length];

        HelperMethods.fillDataArrays(categoriesArr, sumsArr, toView);

        for(int i = 0; i < sumsArr.length; i++){
            yEntrys.add(new PieEntry((float)sumsArr[i], i));
        }

        ArrayList<String> xEntrys = new ArrayList<>(Arrays.asList(categoriesArr));

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Actions Sums");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(12);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);
        colors.add(Color.WHITE);

        pieDataSet.setColors(colors);

        ArrayList<LegendEntry> legendEntries = new ArrayList<>();
        for (int i = 0; i < categoriesArr.length; i++){
            LegendEntry newEntry = new LegendEntry();
            newEntry.formColor = colors.get(i);
            newEntry.label = categoriesArr[i];
            legendEntries.add(newEntry);
        }

        // add customized legend to chart
        Legend legend = pcMain.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setCustom(legendEntries);
        legend.setTextSize(20f);
        legend.setWordWrapEnabled(true);
        legend.setFormSize(15f);
        legend.setFormToTextSpace(7f);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        // legend.setOrientation(Legend.LegendPosition.LEFT_OF_CHART);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pcMain.setData(pieData);
        pcMain.invalidate();
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String actionType = actionTypeGraphsSpinner.getSelectedItem().toString(); // get the selected action type
            String timeSpan = timeSpansGraphsSpinner.getSelectedItem().toString(); // get the selected time span

            // determine the requested class
            if (actionType.equals(getResources().getString(R.string.outcome))){
                CLASS_TO_DISPLAY = Outcome.class;
            } else if (actionType.equals(getResources().getString(R.string.income))){
                CLASS_TO_DISPLAY = Income.class;
            } else {
                CLASS_TO_DISPLAY = Action.class;
            }

            // setup the actions to be displayed
            setupToView(CLASS_TO_DISPLAY, timeSpan);
            addDataSet(); // update the graph
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}