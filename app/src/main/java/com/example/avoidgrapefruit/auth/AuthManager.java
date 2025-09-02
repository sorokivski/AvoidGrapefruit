package com.example.avoidgrapefruit.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.example.avoidgrapefruit.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import lombok.Getter;

@Getter
public class AuthManager {

    private static AuthManager instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private final SignInClient oneTapClient;
    private final BeginSignInRequest signInRequest;

    private AuthManager(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        oneTapClient = Identity.getSignInClient(context);
        signInRequest = new BeginSignInRequest.Builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(false)
                                .build())
                .build();
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

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
        firebaseAuth.signOut();
        oneTapClient.signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // ---- Google Sign-In ----
    public void signInWithGoogle(Activity activity, ActivityResultLauncher<IntentSenderRequest> launcher) {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    launcher.launch(intentSenderRequest);
                })
                .addOnFailureListener(e -> Toast.makeText(activity, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void handleGoogleSignInResult(Intent data, OnCompleteListener<AuthResult> listener) {
        try {
            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
            String idToken = credential.getGoogleIdToken();
            if (idToken != null) {
                AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                firebaseAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(listener);
            }
        } catch (ApiException e) {
            Log.e("AuthManager", "Google sign-in failed", e);
        }
    }
}
