package com.example.avoidgrapefruit.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class VerifyEmailActivity extends AppCompatActivity {

    private Button btnCheckVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        Button btnCheckVerified = findViewById(R.id.btnCheckVerified);

        btnCheckVerified.setOnClickListener(v -> checkEmailVerification());

        // Check if app was opened from a Firebase email verification link
        Uri deepLink = getIntent().getData();
        if (deepLink != null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.isSignInWithEmailLink(deepLink.toString())) {
                // You can retrieve the email from extras or saved state
                String email = getIntent().getStringExtra("email");

                if (email != null) {
                    auth.signInWithEmailLink(email, deepLink.toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    if (user != null && user.isEmailVerified()) {
                                        Toast.makeText(this, "Email verified!", Toast.LENGTH_SHORT).show();
                                        goToHome();
                                    }
                                } else {
                                    Toast.makeText(this, "Verification failed: " +
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        }
    }

    private void checkEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && user.isEmailVerified()) {
                    goToHome();
                } else {
                    Toast.makeText(this, "Not verified yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // update intent
        handleEmailActionLink(intent);
    }

    private void handleEmailActionLink(Intent intent) {
        Uri link = intent.getData();
        if (link != null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String linkStr = link.toString();
            if (auth.isSignInWithEmailLink(linkStr)) {
                // Retrieve the email used during sign-up (we passed it in SignUpActivity)
                String email = getIntent().getStringExtra("email");
                if (email == null) {
                    // fallback if lost
                    email = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                            .getString("pendingEmail", null);
                }

                if (email != null) {
                    auth.signInWithEmailLink(email, linkStr)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    Toast.makeText(this, "Email verified! Logging in...", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this,
                                            "Failed to verify: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        }
    }
}
