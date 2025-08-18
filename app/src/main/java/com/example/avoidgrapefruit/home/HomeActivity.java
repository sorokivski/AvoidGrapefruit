package com.example.avoidgrapefruit.home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.avoidgrapefruit.R;


import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;


import com.example.avoidgrapefruit.auth.GoogleAuth;
import com.example.avoidgrapefruit.auth.LoginActivity;
// It seems ProductActivity is not imported.
// import com.example.avoidgrapefruit.ProductActivity; // Assuming ProductActivity is in this package
import com.example.avoidgrapefruit.products.ProductActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {


    private GoogleAuth googleAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        googleAuth = new GoogleAuth(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());

        // Assuming you have a Button with the ID btnGoToProducts in your activity_home.xml layout
        Button btnGoToProducts = findViewById(R.id.btnGoToProducts);
        btnGoToProducts.setOnClickListener(v -> {
            // Make sure ProductActivity is correctly imported or use the fully qualified name
            // Intent intent = new Intent(HomeActivity.this, com.example.avoidgrapefruit.ProductActivity.class);
            // For now, I'll assume it's in the same package or imported.
            // If ProductActivity is in a different package, ensure it's imported correctly.
            // For example: import com.example.avoidgrapefruit.product.ProductActivity;
            Intent intent = new Intent(HomeActivity.this, ProductActivity.class);
            startActivity(intent);
        });
    }


    private void logoutUser() {
        // Sign out from Firebase directly
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google
        googleAuth.signOut();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}

