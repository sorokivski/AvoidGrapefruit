package com.example.avoidgrapefruit.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.user.UserProfileActivity;
import com.google.firebase.firestore.DocumentSnapshot;

public class HomeActivity extends AppCompatActivity {

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        authManager = AuthManager.getInstance(this);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvEntity = findViewById(R.id.tvEntity);

        if (authManager.isUserLoggedIn()) {
            String email = authManager.getCurrentUser().getEmail();
            tvWelcome.setText("Welcome back, " + email);
        } else {
            tvWelcome.setText("Welcome!");
        }


        authManager.getFirestore().collection("drugs")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String drugName = doc.getString("name");
                        Toast.makeText(this, "First drug: " + drugName, Toast.LENGTH_LONG).show();
                        tvEntity.setText(drugName);
                    } else {
                        Toast.makeText(this, "No drugs found", Toast.LENGTH_SHORT).show();
                        tvEntity.setText("no drugs :((");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching drugs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnProfile = findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));
        btnLogout.setOnClickListener(v -> authManager.logout(this));
    }
}
