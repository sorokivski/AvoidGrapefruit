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

public class SignUpActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private GoogleAuth googleAuth;

    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        googleAuth = new GoogleAuth(this);

        // Views
        editEmail = findViewById(R.id.editEmailSignUp);
        editPassword = findViewById(R.id.editPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);
        TextView tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);
        LinearLayout btnGoogleSignIn = findViewById(R.id.btnCustomGoogle);

        progressBar.setVisibility(View.GONE);

        tvAlreadyHaveAccount.setOnClickListener(view -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });

        btnGoogleSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleAuth.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

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

        btnSignUp.setOnClickListener(view -> {
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
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification().addOnCompleteListener(verifyTask -> {
                                    if (verifyTask.isSuccessful()) {
                                        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                                                .edit()
                                                .putString("pendingEmail", email)
                                                .apply();

                                        Intent intent = new Intent(this, VerifyEmailActivity.class);
                                        intent.putExtra("email", email); // pass email forward
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
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
                        Toast.makeText(this,
                                "Google auth failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
