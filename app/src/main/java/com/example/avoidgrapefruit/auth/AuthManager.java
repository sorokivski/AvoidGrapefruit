package com.example.avoidgrapefruit.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lombok.Getter;

public class AuthManager {

    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    @Getter
    private final FirebaseFirestore firestore;
    private GoogleAuth googleAuth; // optional, init only if needed

    private AuthManager(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    // --- Auth related ---
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void logout(Context context) {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        if (googleAuth != null) {
            googleAuth.signOut();
        }

        // Navigate to LoginActivity and clear back stack
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    public GoogleAuth getGoogleAuth(Activity activity) {
        if (googleAuth == null) {
            googleAuth = new GoogleAuth(activity);
        }
        return googleAuth;
    }

}
