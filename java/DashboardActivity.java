package com.example.expensetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    public static final String USERNAME_INTENT = "user";
    public static final String ACTION_ID_INTENT = "actionId";
    public static final int NEW_ACTION_ACTIVITY = 4;
    public static final int EDIT_ACTIVITY = 9;

    TextView tvBalance, tvGreeting;
    ListView lvActions;

    ActionsListAdapter adapter;
    ArrayList<Action> actions;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvBalance = findViewById(R.id.tvBalance);
        lvActions = findViewById(R.id.lvActions);
        tvGreeting = findViewById(R.id.tvGreeting);

        username = getIntent().getExtras().getString(USERNAME_INTENT); // get the username from the intent

        updateCurrentUsername(); // update the last logged-in username in the general_settings file

        // launch delete confirmation dialog on long click
        lvActions.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                launchDeleteActionDialog(i);
                return true;
            }
        });

        // edit action on single click
        lvActions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(DashboardActivity.this, AddActionActivity.class);
                intent.putExtra(ACTION_ID_INTENT, actions.get(i).getActionId()); // add the edited action's ID
                intent.putExtra(USERNAME_INTENT, username); // add the username of the corresponding user
                startActivityForResult(intent, EDIT_ACTIVITY);
            }
        });

        personalize();
    }

    public void personalize(){
        User user = DatabaseTools.getUser(username); // get the relevant user

        if (user == null){ // ensure the user exists
            return;
        }

        String greeting = getResources().getString(R.string.greeting);
        tvGreeting.setText(String.format(getResources().getString(R.string.greeting), username));
        tvBalance.setText(user.getBalance() + "");

        // determine balance TextView color
        if (user.getBalance() > 0){
            tvBalance.setTextColor(this.getResources().getColor(R.color.incomeColor, null));
        } else if (user.getBalance() < 0){
            tvBalance.setTextColor(this.getResources().getColor(R.color.outcomeColor, null));
        }

        // setup the list view of the user's actions
        actions = DatabaseTools.getActionsOfUser(username);
        adapter = new ActionsListAdapter(this, 0, 0, actions);
        lvActions.setAdapter(adapter);
    }

    private void updateCurrentUsername(){
        SharedPreferences sp = getSharedPreferences(MainActivity.generalSettingsSpName, MODE_PRIVATE); // open the sharedPreference settings file
        SharedPreferences.Editor editor = sp.edit(); // get an editor to the sharedPreference
        editor.putString(MainActivity.generalSettingsSpCurrentUsername, username); // document the last logged in username
        editor.commit(); // save the changes
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = this.getMenuInflater(); // get the menu inflater
        inflater.inflate(R.menu.main_menu, menu); // inflate the menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        // check which item was clicked
        if(item.getItemId() == R.id.optSettings){
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(USERNAME_INTENT, username);
            startActivity(intent);
        } else if (item.getItemId() == R.id.optGraphs){
            Intent intent = new Intent(this, GraphsActivity.class);
            intent.putExtra(USERNAME_INTENT, username);
            startActivity(intent);
        } else if (item.getItemId() == R.id.optNewAction){
            Intent intent = new Intent(this, AddActionActivity.class);
            intent.putExtra(USERNAME_INTENT, username);
            startActivityForResult(intent, NEW_ACTION_ACTIVITY);
        } else if (item.getItemId() == R.id.optSignOut){
            setResult(RESULT_OK);
            finish();
        } else if (item.getItemId() == R.id.optAnalytics){
            Intent intent = new Intent(this, AnalyticsActivity.class);
            intent.putExtra(USERNAME_INTENT, username);
            startActivity(intent);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED){
            return;
        }

        if (requestCode == NEW_ACTION_ACTIVITY || requestCode == EDIT_ACTIVITY){
            personalize();
        }
    }

    private void launchDeleteActionDialog(int index){
        // Action toDelete = actions.get(index);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.deleteDialogTitle)); // set dialog title
        builder.setMessage(getResources().getString(R.string.deleteDialogMessage)); // add subtitle
        builder.setCancelable(true); // whether a tap outside the dialog will close it
        builder.setPositiveButton(getResources().getString(R.string.deleteDialogConfirm), new HandleAlertDialogListener(true, index)); // set "confirm" button text and listener
        builder.setNegativeButton(getResources().getString(R.string.deleteDialogCancel), new HandleAlertDialogListener(false, index)); // set "cancel" button text and listener
        AlertDialog dialog = builder.create(); // create the dialog
        dialog.show();
    }

    private class HandleAlertDialogListener implements DialogInterface.OnClickListener {
        private boolean positive; // holds the identity of the pressed button
        private int index;

        public HandleAlertDialogListener(boolean positive, int index){
            super(); // do all necessary stuff
            this.positive = positive; // set the "rule" of the button
            this.index = index; // the index of the action to be deleted
        }
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            // -1 - the upper button, -2 - the lower button
            // Toast.makeText(DashboardActivity.this, this.positive + " YOU SELECTED: " + which, Toast.LENGTH_SHORT).show();

            if (positive){ // check whether the user confirmed to delete the action
                // remove the action if the user confirmed
                if (!DatabaseTools.deleteAction(actions.get(index))){
                    HelperMethods.showAlertDialog(DashboardActivity.this, getString(R.string.attentionPlease), getString(R.string.actionDeleteError));
                }
                /* actions.remove(index); // remove the action from the list
                adapter.notifyDataSetChanged(); // update the listView */
                personalize();
            }
        }
    }
}