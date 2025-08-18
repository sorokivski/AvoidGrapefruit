package com.example.avoidgrapefruit.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.User;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserAge, tvAllergies, tvMedicalConditions;
    private ImageView ivUserPicture;
    private RecyclerView recyclerDrugs;
    private FloatingActionButton fabAddDrug;
    private UserDrugAdapter adapter;
    private User user;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        authManager = AuthManager.getInstance(this);

        tvUserName = findViewById(R.id.user_name);
        tvUserAge = findViewById(R.id.user_details);
        tvAllergies = findViewById(R.id.tvAllergies);
        tvMedicalConditions = findViewById(R.id.tvMedicalConditions);
        ivUserPicture = findViewById(R.id.user);
        recyclerDrugs = findViewById(R.id.recycler_drugs);
        fabAddDrug = findViewById(R.id.fab_add_drug);

        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> onLogoutClick());

        fabAddDrug.setOnClickListener(v -> openAddDrug());

        recyclerDrugs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserDrugAdapter(new UserDrugAdapter.OnDrugClickListener() {
            @Override
            public void onCardClick(UserDrug drug) {
                openUserDrugSchedule(drug);
            }

            @Override
            public void onNameClick(UserDrug drug) {
                openRealDrugInfo(drug);
            }

            @Override
            public void onEditClick(UserDrug drug) {
                editUserDrug(drug);
            }

            @Override
            public void onDeleteClick(UserDrug drug) {
                deleteUserDrug(drug);
            }
        });
        recyclerDrugs.setAdapter(adapter);

        loadUserData();
    }

    private void loadUserData() {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            authManager.logout(this);
            return;
        }

        authManager.getFirestore().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        redirectToEditProfile();
                        return;
                    }
                    user = doc.toObject(User.class);

                    tvUserName.setText(user.getUsername());
                    tvUserAge.setText(user.getAge() + " years");
                    tvMedicalConditions.setText("Medical Conditions: " + user.getMedicalConditions());
                    tvAllergies.setText("Allergies: " + user.getAllergies());

                    if (user.getUserPicture() != null && !user.getUserPicture().isEmpty()) {
                        try {
                            byte[] decodedBytes = Base64.decode(user.getUserPicture(), Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            ivUserPicture.setImageBitmap(decodedBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ivUserPicture.setImageResource(R.drawable.user);
                    }

                    if (user.getDrugs() != null && !user.getDrugs().isEmpty()) {
                        adapter.setData(new ArrayList<>(user.getDrugs().values()));
                    } else {
                        adapter.setData(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty() || user.getAge() <= 0) {
                        redirectToEditProfile();
                    } else {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("NEW_PROFILE", true);
        startActivity(intent);
        finish();
    }

    private void openEditProfile() {
        startActivity(new Intent(this, EditProfileActivity.class));
    }

    private void openAddDrug() { /* startActivity(new Intent(this, AddDrugActivity.class)); */ }

    private void openUserDrugSchedule(UserDrug drug) {
        Intent i = new Intent(this, UserDrugDetailsActivity.class);
        i.putExtra("USER_DRUG_ID", drug.getDrugId());
        startActivity(i);
    }

    private void openRealDrugInfo(UserDrug drug) { /* ... */ }

    private void editUserDrug(UserDrug drug) {
        Intent i = new Intent(this, EditUserDrugActivity.class);
        i.putExtra("USER_DRUG_ID", drug.getDrugId());
        startActivity(i);
    }

    private void deleteUserDrug(UserDrug drug) {
        String userId = authManager.getCurrentUserId();
        if (userId == null) return;

        authManager.getFirestore()
                .collection("users")
                .document(userId)
                .update("drugs." + drug.getDrugId(), FieldValue.delete())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Drug deleted", Toast.LENGTH_SHORT).show();
                    adapter.removeDrug(drug);
                });
    }

    public void onLogoutClick() {
        authManager.logout(this);
    }
}
