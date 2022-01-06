package com.example.expensetracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignInActivity extends SignActivityOutline {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        etRePass.setVisibility(View.INVISIBLE); // hide the password re-enter field
        tvPasswordStrength.setVisibility(View.INVISIBLE);

        // add click listeners
        btnSignSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    private void signIn(){
        // get the entered credentials
        String username = etUser.getText().toString();
        String passwd = etPass.getText().toString();

        // hash the password and try to authenticate
        try {
            MessageDigest md = MessageDigest.getInstance(DatabaseTools.HASH_FUNCTION); // initialize the cryptography hash
            byte[] result = md.digest(passwd.getBytes(StandardCharsets.UTF_8)); // encrypt the password

            // add the user to the database
            if (DatabaseTools.Authenticate(username, new String(result, StandardCharsets.UTF_8)) == null){
                // issue an error
                Toast.makeText(this, R.string.signInError, Toast.LENGTH_SHORT).show();
                return;
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