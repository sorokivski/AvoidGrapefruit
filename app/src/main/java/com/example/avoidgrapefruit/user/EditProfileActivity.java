package com.example.avoidgrapefruit.user;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.User;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etAge, etMedical, etAllergies;
    private TextView tvEmail;
    private ShapeableImageView ivProfilePicture;
    private Button btnSave;
    private ChipGroup genderChipGroup;

    private Uri selectedImageUri;
    private String userId;
    private String userEmail;
    private User.Gender selectedGender = null;

    private AuthManager authManager;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        authManager = AuthManager.getInstance(this);

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        userEmail = currentUser.getEmail();


        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        etUsername = findViewById(R.id.etUsername);
        etAge = findViewById(R.id.etAge);
        genderChipGroup = findViewById(R.id.genderChipGroup);
        tvEmail = findViewById(R.id.tvEmail);
        etMedical = findViewById(R.id.etMedicalConditions);
        etAllergies = findViewById(R.id.etAllergies);
        btnSave = findViewById(R.id.btnSaveProfile);

        tvEmail.setText(userEmail);

        // Image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        Glide.with(this).load(selectedImageUri).into(ivProfilePicture);
                    }
                }
        );

        ivProfilePicture.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> saveProfile());

        loadUserData();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void loadUserData() {

        authManager.getFirestore().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    User user = doc.toObject(User.class);
                    if (user != null) {
                        etUsername.setText(user.getUsername());
                        etAge.setText(String.valueOf(user.getAge()));
                        tvEmail.setText(user.getEmail());
                        etMedical.setText(user.getMedicalConditions());
                        etAllergies.setText(user.getAllergies());

                        if (user.getGender() == User.Gender.MALE) {
                            genderChipGroup.check(R.id.chipMale);
                        } else if (user.getGender() == User.Gender.FEMALE) {
                            genderChipGroup.check(R.id.chipFemale);
                        }

                        if (user.getUserPicture() != null && !user.getUserPicture().isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(user.getUserPicture(), Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                ivProfilePicture.setImageBitmap(decodedBitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
                );
    }

    private void saveProfile() {
        btnSave.setEnabled(false);

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String ageStr = etAge.getText() != null ? etAge.getText().toString().trim() : "";
        String medical = etMedical.getText() != null ? etMedical.getText().toString().trim() : "";
        String allergies = etAllergies.getText() != null ? etAllergies.getText().toString().trim() : "";

        int checkedId = genderChipGroup.getCheckedChipId();
        selectedGender = (checkedId == R.id.chipMale) ? User.Gender.MALE :
                (checkedId == R.id.chipFemale) ? User.Gender.FEMALE : null;

        if (username.isEmpty() || ageStr.isEmpty() || selectedGender == null) {
            Toast.makeText(this, "Name, age, and gender are required", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true);
            return;
        }

        String base64Image = null;
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        }

        saveUserToFirestore(username, age, selectedGender, medical, allergies, base64Image);
    }

    private void saveUserToFirestore(String username, int age, User.Gender gender,
                                     String medical, String allergies, String base64Image) {
        User user = new User(
                userId,
                username,
                base64Image != null ? base64Image : "",
                age,
                gender,
                userEmail,
                medical,
                allergies,
                null
        );

        authManager.getFirestore().collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditProfileActivity.this, UserProfileActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
