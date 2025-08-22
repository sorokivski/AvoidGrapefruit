package com.example.avoidgrapefruit.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.user_drugs.AddUserDrugActivity;
import com.example.avoidgrapefruit.user_drugs.EditUserDrugActivity;
import com.example.avoidgrapefruit.user_drugs.UserDrugAdapter;
import com.example.avoidgrapefruit.entity.User;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.example.avoidgrapefruit.user_drugs.UserDrugInfoActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserAge, tvAllergies, tvMedicalConditions;
    private ImageView ivUserPicture;
    private UserDrugAdapter drugAdapter;
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
        FloatingActionButton fabAddDrug = findViewById(R.id.fab_add_drug);

        ImageButton btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> onLogoutClick());

        fabAddDrug.setOnClickListener(v -> openAddDrug());

        RecyclerView recyclerDrugs = findViewById(R.id.recycler_drugs);
        recyclerDrugs.setLayoutManager(new LinearLayoutManager(this));

        drugAdapter = new UserDrugAdapter(this::openUserDrug);

        recyclerDrugs.setAdapter(drugAdapter);

        ItemTouchHelper.SimpleCallback simpleCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAbsoluteAdapterPosition();
                        UserDrug drug = drugAdapter.getDrugAt(position);

                        if (direction == ItemTouchHelper.LEFT) {
                            deleteUserDrug(drug);
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            editUserDrug(drug);
                        }

                        drugAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c,
                                            @NonNull RecyclerView recyclerView,
                                            @NonNull RecyclerView.ViewHolder viewHolder,
                                            float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {

                        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY,
                                actionState, isCurrentlyActive)
                                .addSwipeLeftBackgroundColor(ContextCompat.getColor(UserProfileActivity.this, R.color.dark_grey))

                                .addSwipeLeftActionIcon(R.drawable.ic_delete)
                                .addSwipeRightBackgroundColor(ContextCompat.getColor(UserProfileActivity.this, R.color.accent_green))
                                .addSwipeRightActionIcon(R.drawable.ic_edit_24dp)
                                .create()
                                .decorate();

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerDrugs);

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

                    authManager.getFirestore()
                            .collection("users")
                            .document(userId)
                            .collection("drugs")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<UserDrug> drugs = new ArrayList<>();
                                for (DocumentSnapshot dr : querySnapshot.getDocuments()) {
                                    UserDrug drug = dr.toObject(UserDrug.class);
                                    drugs.add(drug);
                                }

                                drugAdapter.setData(drugs);
                            })
                            .addOnFailureListener(e -> {
                                drugAdapter.setData(new ArrayList<>());
                            });

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



    public void onLogoutClick() {
        authManager.logout(this);
    }

    private void openAddDrug() {
        Intent intent = new Intent(this, AddUserDrugActivity.class);
        startActivity(intent);
    }

    private void openUserDrug(UserDrug drug) {
        Intent intent = new Intent(this, UserDrugInfoActivity.class);
        intent.putExtra("uuid", drug.getUuid());
        intent.putExtra("drugId", drug.getDrugId());
        startActivity(intent);
    }



    private void editUserDrug(UserDrug drug) {
        Intent intent = new Intent(this, EditUserDrugActivity.class);
        intent.putExtra("drugId", drug.getUuid());
        startActivity(intent);

    }

    private void deleteUserDrug(UserDrug drug) {
        String userId = authManager.getCurrentUser().getUid();
        authManager.getFirestore()
                .collection("users")
                .document(userId)
                .collection("drugs")
                .document(drug.getUuid()) // Firestore doc id
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.wtf( " DRUG TO DELETE: ", String.valueOf(drug));
                    drugAdapter.removeDrug(drug);
                    Toast.makeText(this, "Drug deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
