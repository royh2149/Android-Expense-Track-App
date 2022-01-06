package com.example.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignUpActivity extends SignActivityOutline {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // add click listeners
        btnSignSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                indicatePass();
            }
        });
    }

    private void indicatePass(){
        String s = ""; // the text to indicate password strength to the user in real time
        int strength = HelperMethods.PasswordStrength(etPass.getText().toString()); // calculate password strength
        if (strength % 10 == 2){ // check length
            s = "Length okay, ";
        }else {
            s = "Too short, ";
        }

        if (strength / 10 == 3){ // very strong
            s += "Strong enough!";
            tvPasswordStrength.setTextColor(Color.GREEN);
        }else if (strength / 10 == 2){ // medium strength
            s += "Medium strength!";
            tvPasswordStrength.setTextColor(Color.YELLOW);
        }else { // weak password
            s += "Very weak!";
            tvPasswordStrength.setTextColor(Color.RED);
        }

        tvPasswordStrength.setText(s);
    }

    private void signUp(){
        // get the entered credentials
        String username = etUser.getText().toString();
        String passwd = etPass.getText().toString();
        String repass = etRePass.getText().toString();

        // ensure no field is empty - if so, stop signing up process
        if(username.isEmpty() || passwd.isEmpty()){
            Toast.makeText(this, R.string.emptyInput, Toast.LENGTH_SHORT).show();
            return;
        }

        // stop the signing up process if the 2 passwords are different
        if (!passwd.equals(repass)){
            Toast.makeText(this, R.string.passwdMismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        // hash the password
        try {
            MessageDigest md = MessageDigest.getInstance(DatabaseTools.HASH_FUNCTION); // initialize the cryptography hash
            byte[] result = md.digest(passwd.getBytes(StandardCharsets.UTF_8)); // encrypt the password
            User newUser = new User(username, new String(result, StandardCharsets.UTF_8)); // create the new user

            // add the user to the database
            if (!DatabaseTools.addUser(newUser.getUsername(), newUser.getPassword(), newUser.getBalance())){
                // issue an error
                Toast.makeText(this, R.string.userAddError, Toast.LENGTH_SHORT).show();
            }
        } catch (NoSuchAlgorithmException e) { // in case of error, inform the user
            Toast.makeText(this, R.string.userAddError, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(DashboardActivity.USERNAME_INTENT, username);
        startActivityForResult(intent, SIGN_OUT);
    }
}