package com.example.expensetracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class AddActionActivity extends AppCompatActivity {

    public static final int CAMERA_ACTIVITY = 6;

    Spinner spCategory;
    RadioButton rbIncome, rbOutcome;
    EditText etSum, etDesc;
    ImageButton ibPicture;

    Bitmap bitmap;
    String username;
    ObjectId actionId;

    int selectedYear = -1, selectedMonth = -1, selectedDay = -1;
    Button btnDate;

    boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_action);

        // default image and username
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        username = getIntent().getExtras().getString(DashboardActivity.USERNAME_INTENT);

        // get the widgets
        rbIncome = findViewById(R.id.rbIncome);
        rbOutcome = findViewById(R.id.rbOutcome);
        etSum = findViewById(R.id.etSum);
        etDesc = findViewById(R.id.etDesc);
        ibPicture = findViewById(R.id.ibPicture);
        btnDate = findViewById(R.id.btnDate);

        // setup the date-setting number pickers
        //setupDatePicker();

        // setup the categories spinner
        spCategory = findViewById(R.id.spCategory);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        // check if the activity's goal is to edit an existing action
        actionId = (ObjectId) getIntent().getExtras().get(DashboardActivity.ACTION_ID_INTENT);

        if (actionId == null){
            isEdit = false;

            String extra = getIntent().getExtras().getString(ActionSMSReceiver.INTENT_DESC);

            if (extra != null){
                String[] data = extra.split(":");
                String description = data[0];
                int sum = Integer.parseInt(data[1]);
                long messageTimestamp = Long.parseLong(data[2]);

                etSum.setText(sum + "");
                etDesc.setText(description);

                // infer the date of the action
                LocalDateTime messageDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(messageTimestamp), TimeZone.getDefault().toZoneId());
                selectedYear = messageDate.getYear();
                selectedMonth = messageDate.getMonthValue();
                selectedDay = messageDate.getDayOfMonth();
                btnDate.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);

                if (sum > 0){
                    rbIncome.setChecked(true);
                } else if (sum < 0) {
                    rbOutcome.setChecked(true);
                    etSum.setText((-sum) + "");
                }

                SharedPreferences sp = getSharedPreferences(MainActivity.generalSettingsSpName, MODE_PRIVATE);
                username = sp.getString(MainActivity.generalSettingsSpCurrentUsername, "idan");
            }

        } else {
            isEdit = true;
            specifyDetails();
        }
    }

    public void submit(View view){
        if (!validateInput()){
            HelperMethods.showAlertDialog(this, getString(R.string.attentionPlease), getString(R.string.emptyInput));
            return;
        }

        Action action = null;

        // LocalDateTime date = LocalDateTime.of(npYear.getValue(), npMonth.getValue(), npDay.getValue(), 12, 0);
        LocalDateTime date = LocalDateTime.of(selectedYear, selectedMonth, selectedDay, 12, 0);
        String category = spCategory.getSelectedItem().toString();
        String desc = etDesc.getText().toString();
        String image = ImageUtils.bitmapToString(bitmap);
        double sum = Double.parseDouble(etSum.getText().toString());

        if (rbIncome.isChecked()){
            Income income = new Income(sum, category, desc, image, username, date);
            action = income;
        } else if (rbOutcome.isChecked()){
            Outcome outcome = new Outcome(sum, category, desc, image, username, date);
            action = outcome;
        } else { // in case no action type was selected
            // issue an error an exit the function
            HelperMethods.showAlertDialog(this, getString(R.string.attentionPlease), getString(R.string.noActionTypeError));
            return;
        }

        // determine the necessary action
        if (isEdit){
            action.setActionId(actionId);
            if (!DatabaseTools.updateAction(action)){ // try to add to the database, if failed, then wait here
                HelperMethods.showAlertDialog(this, getString(R.string.attentionPlease), getString(R.string.actionUpdateError));
                return;
            }
        } else {
            if (!DatabaseTools.insertAction(action)){ // try to add to the database, if failed, then wait here
                HelperMethods.showAlertDialog(this, getString(R.string.attentionPlease), getString(R.string.actionAddError));
                return;
            }
        }

        setResult(RESULT_OK);
        finish();
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void launchCamera(View view){
        startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_ACTIVITY);
    }

    public void launchSelectDateDialog(View view){
        DatePickerDialog dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                selectedYear = year;
                selectedMonth = month + 1;
                selectedDay = day;
                btnDate.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED){
            return;
        }

        if (requestCode == CAMERA_ACTIVITY){
            Bitmap newBitmap = (Bitmap) data.getExtras().get("data");
            bitmap = newBitmap;
            BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
            ibPicture.setBackground(bitmapDrawable);
        }

    }

    /* private void setupDatePicker(){
        npDay = findViewById(R.id.npDay);
        npMonth = findViewById(R.id.npMonth);
        npYear = findViewById(R.id.npYear);

        LocalDateTime dateTime = LocalDateTime.now();

        // set min-max values, set default values to today
        npDay.setMaxValue(31);
        npDay.setMinValue(1);
        npDay.setValue(dateTime.getDayOfMonth());

        npMonth.setMaxValue(12);
        npMonth.setMinValue(1);
        npMonth.setValue(dateTime.getMonthValue());

        npYear.setMinValue(1974);
        npYear.setMaxValue(2100);
        npYear.setValue(dateTime.getYear());
    } */


    /*
    NumberPicker npDay, npMonth, npYear;
     */
    private void specifyDetails(){
        Action action = DatabaseTools.getAction(actionId); // query the database for the requested action
        if (action == null){
            HelperMethods.showAlertDialog(this, getString(R.string.attentionPlease), getString(R.string.connectionError));
            return;
        }

        // set the right category in the spinner
        spCategory.setSelection(HelperMethods.indexOfArray(getResources().getStringArray(R.array.categories), action.getCategory()));

        // specify whether the action is an income or an outcome
        if (action instanceof Income){
            rbIncome.setChecked(true);
        } else {
            rbOutcome.setChecked(true);
        }

        // specify the sum and the description of the action
        etSum.setText(action.getSum() + "");
        etDesc.setText(action.getDesc());
        btnDate.setText(action.getDate().getDayOfMonth() + "/" + action.getDate().getMonthValue() + "/" + action.getDate().getYear());

        // set the bitmap on the button
        Bitmap bitmap = ImageUtils.stringToBitmap(action.getImage());
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        ibPicture.setBackground(bitmapDrawable);

        // set the date
        LocalDateTime dateTime = action.getDate();
        /* npYear.setValue(dateTime.getYear());
        npMonth.setValue(dateTime.getMonthValue());
        npDay.setValue(dateTime.getDayOfMonth()); */
        selectedYear = dateTime.getYear();
        selectedMonth = dateTime.getMonthValue();
        selectedDay = dateTime.getDayOfMonth();

    }

    private boolean validateInput(){
        // check empty fields
        if (etDesc.getText().toString().isEmpty() || etSum.getText().toString().isEmpty()){
            return false;
        }

        if (selectedYear < 0 || selectedMonth < 0 || selectedDay < 0){
            return false;
        }

        return true;
    }
}