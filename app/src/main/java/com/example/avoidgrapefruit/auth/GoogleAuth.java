package com.example.avoidgrapefruit.auth;

import static android.provider.Settings.System.getString;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.example.avoidgrapefruit.R;
import com.google.android.gms.tasks.Task;

public class GoogleAuth {

    private final GoogleSignInClient googleSignInClient;


    public GoogleAuth(Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public Intent getSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    @Nullable
    public GoogleSignInAccount getAccountFromIntent(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            return task.getResult(ApiException.class);
        } catch (ApiException e) {
            Log.e("GoogleAuth", "Sign-in failed", e);
            return null;
        }
    }

    public void signOut() {
        googleSignInClient.signOut();
    }
}
