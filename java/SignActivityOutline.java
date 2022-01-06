package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class SignActivityOutline extends AppCompatActivity {

    public static final int SIGN_OUT = 7;

    EditText etUser, etPass, etRePass;
    ImageButton btnSignSubmit, btnSignBack;
    TextView tvPasswordStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_sign_in);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        etRePass = findViewById(R.id.etRePass);
        tvPasswordStrength = findViewById(R.id.tvPasswordStrength);
        btnSignSubmit = findViewById(R.id.btnSignSubmit);
        btnSignBack = findViewById(R.id.btnSignBack);

        // add click listener for the back button
        // if the back button was pressed, close the current activity with the CANCELED result
        btnSignBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED){
            return;
        }

        if (requestCode == SIGN_OUT){
            finish();
        }
    }
}
