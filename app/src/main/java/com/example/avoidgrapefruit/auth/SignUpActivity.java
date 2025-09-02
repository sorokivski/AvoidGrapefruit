package com.example.avoidgrapefruit.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    private AuthManager authManager;
    private ActivityResultLauncher<IntentSenderRequest> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        authManager = AuthManager.getInstance(this);

        editEmail = findViewById(R.id.editEmailSignUp);
        editPassword = findViewById(R.id.editPasswordSignUp);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        TextView tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);
        LinearLayout btnGoogleSignIn = findViewById(R.id.btnCustomGoogle);

        progressBar.setVisibility(View.GONE);

        // --- Email sign up ---
        btnSignUp.setOnClickListener(view -> signUpWithEmail());

        tvAlreadyHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        // --- Google sign up ---
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        authManager.handleGoogleSignInResult(result.getData(), task -> {
                            if (task.isSuccessful()) {
                                navigateToHome();
                            } else {
                                Toast.makeText(this, "Google sign-up failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        btnGoogleSignIn.setOnClickListener(v ->
                authManager.signInWithGoogle(this, googleSignInLauncher));
    }

    private void signUpWithEmail() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter a valid email");
            editEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            editPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                if (verifyTask.isSuccessful()) {
                                    getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                            .edit()
                                            .putString("pendingEmail", email)
                                            .apply();

                                    Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(this, VerifyEmailActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Sign up failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
