package com.example.avoidgrapefruit.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;

    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    private AuthManager authManager;
    private ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        authManager = AuthManager.getInstance(this);

        editEmail = findViewById(R.id.enterLogin);
        editPassword = findViewById(R.id.enterPassword);
        progressBar = findViewById(R.id.progressBar);

        // Google Sign-In launcher
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        authManager.handleGoogleSignInResult(result.getData(), task -> {
                            if (task.isSuccessful()) {
                                navigateToHome();
                            } else {
                                Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        findViewById(R.id.btnCustomGoogle).setOnClickListener(v ->
                authManager.signInWithGoogle(this, googleSignInLauncher));

        findViewById(R.id.btnLogin).setOnClickListener(v -> loginWithEmail());
        findViewById(R.id.tvSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void loginWithEmail() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter valid email");
            return;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must be longer than 6 symbols");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            navigateToHome();
                        } else {
                            Toast.makeText(this, "Please verify your email.", Toast.LENGTH_LONG).show();
                            firebaseAuth.signOut();
                        }
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
