package com.example.expensetracker;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserPassFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserPassFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String username;
    private String mParam2;

    Button btnLaunchUpdatePasswordDialog, btnLaunchUpdateUsernameDialog;

    public UserPassFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserPassFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserPassFragment newInstance(String param1, String param2) {
        UserPassFragment fragment = new UserPassFragment();
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
        View container = inflater.inflate(R.layout.fragment_user_pass, vgContainer, false);

        // get the buttons
        btnLaunchUpdatePasswordDialog = container.findViewById(R.id.btnLaunchUpdatePasswordDialog);
        btnLaunchUpdateUsernameDialog = container.findViewById(R.id.btnLaunchUpdateUsernameDialog);

        // assign on-click actions
        btnLaunchUpdatePasswordDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchUpdatePasswordDialog(view);
            }
        });

        btnLaunchUpdateUsernameDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchUpdateUsernameDialog(view);
            }
        });

        return container;
    }

    public void launchUpdatePasswordDialog(View view){
        Dialog dialog = new Dialog(this.getContext()); // create the dialog
        dialog.setContentView(R.layout.dialog_update_password); // setup the XML layout
        dialog.setCancelable(true); // close the dialog once the user clicks outside it
        dialog.setTitle(getString(R.string.updatePassword));

        // get the widgets
        Button btnDUPSubmit = dialog.findViewById(R.id.btnDUUSubmit);
        Button btnDUPCancel = dialog.findViewById(R.id.btnDUUCancel);
        EditText etPasswordEnsure = dialog.findViewById(R.id.etCurrentPasswordEnsure);
        EditText etNewPassword = dialog.findViewById(R.id.etNewPassword);
        EditText etNewPasswordConfirm = dialog.findViewById(R.id.etNewPasswordConfirm);

        // specify on-click actions
        btnDUPCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // close the dialog
            }
        });

        btnDUPSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etNewPassword.getText().toString().isEmpty()){ // ensure no empty username
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.emptyInput));
                    return;
                }

                // stop the updating process if the 2 passwords are different
                if (!etNewPassword.getText().toString().equals(etNewPasswordConfirm.getText().toString())){
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.passwdMismatch));
                    return;
                }

                // disable more clicks to prevent unexpected exit
                btnDUPSubmit.setClickable(false);
                btnDUPCancel.setClickable(false);
                dialog.setCancelable(false);

                String passwd = etPasswordEnsure.getText().toString();
                String newPasswd = etNewPassword.getText().toString();
                // encrypt the entered password
                try {
                    MessageDigest md = MessageDigest.getInstance(DatabaseTools.HASH_FUNCTION); // initialize the cryptography hash
                    byte[] result = md.digest(passwd.getBytes(StandardCharsets.UTF_8)); // encrypt the original password
                    passwd = new String(result, StandardCharsets.UTF_8);

                    byte[] newResult = md.digest(newPasswd.getBytes(StandardCharsets.UTF_8)); // encrypt the new password
                    newPasswd = new String(newResult, StandardCharsets.UTF_8);
                } catch (NoSuchAlgorithmException e) {
                    // HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.couldNotEncrypt));
                    return;
                }

                if (DatabaseTools.Authenticate(username, passwd) != null){
                    // change the password only if the password is correct
                    if (DatabaseTools.changePassword(username, newPasswd)){
                        HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.passwordChangedSuccessfully));
                        dialog.dismiss(); // close the dialog once finished
                    } else {
                        HelperMethods.showAlertDialog(getContext(), getResources().getString(R.string.attentionPlease), getResources().getString(R.string.puUpdateError));
                    }
                } else {
                    HelperMethods.showAlertDialog(getContext(), getResources().getString(R.string.attentionPlease), getResources().getString(R.string.puUpdateError));

                    // re-enable more clicks
                    btnDUPSubmit.setClickable(true);
                    btnDUPCancel.setClickable(true);
                    dialog.setCancelable(true);
                }
            }
        });

        dialog.show(); // launch the dialog
    }

    public void launchUpdateUsernameDialog(View view){
        Dialog dialog = new Dialog(this.getContext()); // create the dialog
        dialog.setContentView(R.layout.dialog_update_username); // setup the XML layout
        dialog.setCancelable(true); // close the dialog once the user clicks outside it
        dialog.setTitle(getString(R.string.updateUsername));

        // get the widgets
        Button btnDUUSubmit = dialog.findViewById(R.id.btnDUUSubmit);
        Button btnDUUCancel = dialog.findViewById(R.id.btnDUUCancel);
        EditText etPasswordEnsure = dialog.findViewById(R.id.etPasswordEnsure);
        EditText etNewUsername = dialog.findViewById(R.id.etNewUsername);

        // specify on-click actions
        btnDUUCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss(); // close the dialog
            }
        });

        btnDUUSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etNewUsername.getText().toString().isEmpty()){ // ensure no empty username
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease),  getString(R.string.emptyInput));
                    return;
                }

                // disable more clicks to prevent unexpected exit
                btnDUUSubmit.setClickable(false);
                btnDUUCancel.setClickable(false);
                dialog.setCancelable(false);

                String passwd = etPasswordEnsure.getText().toString();
                // encrypt the entered password
                try {
                    MessageDigest md = MessageDigest.getInstance(DatabaseTools.HASH_FUNCTION); // initialize the cryptography hash
                    byte[] result = md.digest(passwd.getBytes(StandardCharsets.UTF_8)); // encrypt the password
                    passwd = new String(result, StandardCharsets.UTF_8);
                } catch (NoSuchAlgorithmException e) {
                    return;
                }

                if (DatabaseTools.Authenticate(username, passwd) != null){
                    // change the username only if the password is correct
                    if (DatabaseTools.changeUsername(username, etNewUsername.getText().toString())){
                        HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.usernameChangedSuccessfully));
                        dialog.dismiss(); // close the dialog once finished
                    } else {
                        HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.existingUserError));
                    }
                } else {
                    HelperMethods.showAlertDialog(getContext(), getString(R.string.attentionPlease), getString(R.string.wrongPassword));

                    // re-enable more clicks
                    btnDUUSubmit.setClickable(true);
                    btnDUUCancel.setClickable(true);
                    dialog.setCancelable(true);
                }
            }
        });

        dialog.show(); // launch the dialog
    }
}