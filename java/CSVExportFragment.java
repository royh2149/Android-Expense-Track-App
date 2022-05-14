package com.example.expensetracker;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CSVExportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CSVExportFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final int CREATE_FILE_ACTIVITY = 6;

    // TODO: Rename and change types of parameters
    private String username;
    private String mParam2;

    Spinner timeSpansSpinner;
    Spinner actionTypeSpinner;
    Button btnSendCSV;

    boolean isExternalStorageAvailable, isExternalStorageWritable;

    public CSVExportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CSVExportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CSVExportFragment newInstance(String param1, String param2) {
        CSVExportFragment fragment = new CSVExportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        checkPerms();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_c_s_v_export, container, false);
        timeSpansSpinner = view.findViewById(R.id.time_spans_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.timeSpans, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        timeSpansSpinner.setAdapter(adapter);

        actionTypeSpinner = view.findViewById(R.id.action_type_spinner);
        ArrayAdapter<CharSequence> secondAdapter = ArrayAdapter.createFromResource(this.getContext(), R.array.actions, android.R.layout.simple_spinner_item);
        secondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionTypeSpinner.setAdapter(secondAdapter);

        btnSendCSV = view.findViewById(R.id.btnSendCSV);
        btnSendCSV.setOnClickListener(this::sendCSVFile);

        return view;
    }

    public void sendCSVFile(View view){
        if (!isExternalStorageWritable){ // ensure we can create a file
            HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.storagePermissionError));
            return;
        }

        try {
            createCSVFile();
        } catch (Exception e){
            HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.fileCreationError));
        }
    }

    private void checkPerms(){
        String state = Environment.getExternalStorageState(); // the state describes the current situation of the external storage

        if (Environment.MEDIA_MOUNTED.equals(state)){ // check if we have read and write
            isExternalStorageAvailable = true;
            isExternalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){ // check if we have read only
            isExternalStorageAvailable = true;
            isExternalStorageWritable = false;
        } else { // something is wrong. We can neither read nor write
            isExternalStorageAvailable = false;
            isExternalStorageWritable = false;
        }
    }

    private void createCSVFile(){
        String filename = username +"-report-"+ LocalDateTime.now().toString()+".csv";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT); // create the intent that will ask the user for confirmation to save the file
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv"); // specify file type
        intent.putExtra(Intent.EXTRA_TITLE, "ETCSVF/" + filename); // set the filename
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, "");
        startActivityForResult(intent, CREATE_FILE_ACTIVITY); // run the intent
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK){
            return;
        }

        if (requestCode == CREATE_FILE_ACTIVITY){
            double balance = 0.0; // hold the total balance
            ArrayList<Action> actions = DatabaseTools.getActionsOfUser(username); // get all the actions

            Uri uri = data.getData(); // get the filename
            try {
                // open the output stream
                OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);

                filterActions(actions); // list only files that match the specified criteria

                // write the first line - the names of the column
                outputStream.write("Type,Date,Description,Category,Sum\n".getBytes(StandardCharsets.UTF_8));
                for (Action action : actions){ // iterate through all the action
                    double sign = (action instanceof Outcome) ? -1 : 1; // determine the sign of the current action
                    balance += action.getSum() * sign; // take into account the current action
                    outputStream.write(HelperMethods.convertToCSV(action).getBytes(StandardCharsets.UTF_8)); // write the current action to the file
                }

                // write the total balance
                outputStream.write(("Total Balance,,,," + balance).getBytes(StandardCharsets.UTF_8));
                outputStream.close(); // close the file
                shareCSVFile(uri);
            } catch (IOException e) { // issue an error in case of failure
                HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.fileCreationError));
            }
        }
    }

    private void filterActions(ArrayList<Action> actions){
        LocalDateTime startDate = HelperMethods.getStartDate(timeSpansSpinner.getSelectedItem().toString(), getContext());
        int actionType = actionTypeSpinner.getSelectedItemPosition(); // get the index of the current item

        for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext();){ // traverse the actions list
            Action action = iterator.next(); // get the current action
            if (action.getDate().isBefore(startDate)){ // remove it from the list if is not in the requested time span
                iterator.remove();
                continue;
            }

            // remove it from the list if it is not the requested type
            switch (actionType){
                case 0: // remove outcomes when income are desired
                    if (action instanceof Outcome){
                        iterator.remove();
                    }
                    break;
                case 1: // remove incomes when outcomes are desired
                    if (action instanceof Income){
                        iterator.remove();
                    }
                    break;
            }
        }
    }

    private void shareCSVFile(Uri uri){
        Intent intentShareFile = new Intent(Intent.ACTION_SEND); // create the sharing intent

        intentShareFile.setType("application/csv"); // specify file type
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri); // add the file itself

        // set subject & body text if necessary
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, R.string.fileShareSubject);
        intentShareFile.putExtra(Intent.EXTRA_TEXT, R.string.fileShareText);

        // let the user choose how to share the file
        startActivity(Intent.createChooser(intentShareFile, getResources().getString(R.string.shareFile)));
    }
}