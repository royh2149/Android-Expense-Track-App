package com.example.expensetracker;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonthlyStatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonthlyStatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String username;
    private String mParam2;

    LinearLayout svMonthlyCategories;

    public MonthlyStatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonthlyStatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonthlyStatsFragment newInstance(String param1, String param2) {
        MonthlyStatsFragment fragment = new MonthlyStatsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vgContainer,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View container = inflater.inflate(R.layout.fragment_monthly_stats, vgContainer, false);

        svMonthlyCategories = container.findViewById(R.id.svMonthlyCategories); // get the layout of the buttons
        String[] categories = getResources().getStringArray(R.array.categories); // get the different categories

        for (int i = 0; i < categories.length; i++){ // iterate through all the categories
            // create the button
            Button btn = new Button(getContext());
            btn.setText(categories[i]);
            btn.setTag(categories[i]);

            // set a listener for the button
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchUpdateBarrierDialog(view.getTag().toString());
                }
            });

            // add the button to the layout
            svMonthlyCategories.addView(btn);

        }

        return container;
    }

    private void launchUpdateBarrierDialog(String category){
        Dialog dialog = new Dialog(this.getContext()); // create the dialog
        dialog.setContentView(R.layout.dialog_set_barrier); // setup the XML layout
        dialog.setCancelable(true); // close the dialog once the user clicks outside it
        dialog.setTitle(getString(R.string.monthlyBarrier));

        // get the widgets
        Button btnSubmit = dialog.findViewById(R.id.btnDBSSubmit);
        Button btnCancel = dialog.findViewById(R.id.btnDBSCancel);
        EditText etBarrierSet = dialog.findViewById(R.id.etBarrierSet);
        TextView tvCurrentCategoryBarrier = dialog.findViewById(R.id.tvCurrentCategoryBarrier);

        tvCurrentCategoryBarrier.setText(category); // specify category

        // specify on-click actions
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // close the dialog
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etBarrierSet.getText().toString().isEmpty()){ // ensure no empty input
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getResources().getString(R.string.emptyInput));
                    return;
                }

                // disable more clicks to prevent unexpected exit
                btnSubmit.setClickable(false);
                btnCancel.setClickable(false);
                dialog.setCancelable(false);

                // get the spending barrier for the current category
                int barrier = Integer.parseInt(etBarrierSet.getText().toString());

                if (barrier < 0){ // ensure the barrier typed by the user is not negative
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.negativeBarrier));
                    return;
                }

                // specify user's barrier in share preference
                updateSharedPreferenceBarrier(barrier, category);
                dialog.dismiss(); // close the dialog
            }
        });

        dialog.show(); // launch the dialog
    }

    private void updateSharedPreferenceBarrier(int barrier, String category){
        SharedPreferences storage = getContext().getSharedPreferences(HelperMethods.getSpFilename(username), MODE_PRIVATE); // access to shared preference
        SharedPreferences.Editor editor = storage.edit(); // get an editor for the sharedPreference
        editor.putInt(category, barrier); // save the barrier to the sharedPreference
        editor.commit(); // save the changes
    }
}