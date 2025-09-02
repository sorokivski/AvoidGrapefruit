package com.example.avoidgrapefruit;

import com.example.avoidgrapefruit.auth.SignUpActivity;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.interactions.InteractionsActivity;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthManager authManager = AuthManager.getInstance(this);
        if (!authManager.isUserLoggedIn()) {
            startActivity(new Intent(this, SignUpActivity.class));
        } else {
            startActivity(new Intent(this, InteractionsActivity.class));
        }
        finish();
    }
}
