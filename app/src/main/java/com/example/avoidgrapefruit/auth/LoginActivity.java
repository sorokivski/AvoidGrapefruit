package com.example.avoidgrapefruit.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.home.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private GoogleAuth googleAuth;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        googleAuth = new GoogleAuth(this);

        // Initialize views
        editEmail = findViewById(R.id.enterLogin);
        editPassword = findViewById(R.id.enterPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Go to Sign Up
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Custom Google Sign-In button (assumes it's a LinearLayout)
        LinearLayout btnGoogleSignIn = findViewById(R.id.btnCustomGoogle);
        btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleAuth.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Modern Google Sign-In result handler
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        GoogleSignInAccount account = googleAuth.getAccountFromIntent(result.getData());
                        if (account != null) {
                            firebaseAuthWithGoogle(account.getIdToken());
                        } else {
                            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Email/password login
        btnLogin.setOnClickListener(view -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Enter valid email");
                editEmail.requestFocus();
                return;
            }

            if (password.length() < 6) {
                editPassword.setError("Password must be longer than 6 symbols");
                editPassword.requestFocus();
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
                                Toast.makeText(this,
                                        "Please verify your email before logging in.",
                                        Toast.LENGTH_LONG).show();
                                firebaseAuth.signOut();
                            }
                        } else {
                            Toast.makeText(this, "Login failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();

                            firebaseAuth.fetchSignInMethodsForEmail(email)
                                    .addOnCompleteListener(methodsTask -> {
                                        boolean userExists = methodsTask.isSuccessful() &&
                                                methodsTask.getResult() != null &&
                                                !methodsTask.getResult().getSignInMethods().isEmpty();
                                        if (userExists) {
                                            Toast.makeText(this,
                                                    "Forgot your password? Try resetting it.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this,
                                                    "No account found with this email. Please check or sign up.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    });
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        navigateToHome();
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
