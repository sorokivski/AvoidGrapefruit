package com.example.avoidgrapefruit.user_drugs;


import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.drugs.DrugInfoHelper;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class UserDrugInfoActivity extends AppCompatActivity {

    private TextView tvDrugName, tvDosage, tvDates, tvAmountPerDay, tvActiveStatus;
    private LinearLayout intakeTimesContainer;
    private ScrollView globalDrugScroll;


    private View sectionDescription, sectionDosage, sectionPrecautions, sectionMealTiming;
    private ChipGroup chipGroupTags, chipGroupBrands, chipGroupSynonyms;

    private FirebaseFirestore db;
    private String userId;
    private String uuid;

    private UserDrug userDrug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_info);

        AuthManager authManager = AuthManager.getInstance(this);
        db = authManager.getFirestore();

        userId = authManager.getCurrentUserId();
        String drugId = getIntent().getStringExtra("drugId");
        uuid  = getIntent().getStringExtra("uuid");


        if (userId == null || drugId == null) {
            Toast.makeText(this, "Missing user or drug ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserDrug();
    }

    private void initViews() {
        tvDrugName = findViewById(R.id.tvDrugName);
        tvDosage = findViewById(R.id.tvDosage);
        tvDates = findViewById(R.id.tvDates);
        tvAmountPerDay = findViewById(R.id.tvAmountPerDay);
        tvActiveStatus = findViewById(R.id.tvActiveStatus);
        intakeTimesContainer = findViewById(R.id.intakeTimesContainer);

        globalDrugScroll = findViewById(R.id.globalDrugScroll);
        sectionDescription = findViewById(R.id.sectionDescription);
        sectionDosage = findViewById(R.id.sectionDosage);
        sectionMealTiming = findViewById(R.id.sectionMealTiming);
        sectionPrecautions = findViewById(R.id.sectionPrecautions);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        chipGroupBrands = findViewById(R.id.chipGroupBrands);
        chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);


        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadUserDrug() {
        db.collection("users")
                .document(userId)
                .collection("drugs")
                .document(uuid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Drug not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    userDrug = doc.toObject(UserDrug.class);
                    if (userDrug == null) {
                        Toast.makeText(this, "Failed to parse drug", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    displayUserDrugInfo(userDrug);
                    //same uuid and drugId if user added non existent in drugs collections drug
                    if (!userDrug.getUuid().equals(userDrug.getDrugId())) {
                        loadGlobalDrug(userDrug.getDrugId());
                    }
                });
    }

    private void displayUserDrugInfo(UserDrug drug) {
        tvDrugName.setText(drug.getDrugName());
        tvDosage.setText("Dosage: " + drug.getDosage());
        tvAmountPerDay.setText("Amount per day: " + drug.getAmountPerDay());
        tvActiveStatus.setText(drug.isActive() ? "Active" : "Inactive");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String start = sdf.format(drug.getStartDate());
        String end = sdf.format(drug.getEndDate());
        tvDates.setText("Start: " + start + " → End: " + end);

        intakeTimesContainer.removeAllViews();
        for (UserDrug.IntakeTime time : drug.getIntakeTimes()) {
            TextView timeView = new TextView(this);
            timeView.setText("• " + time.name());
            intakeTimesContainer.addView(timeView);
        }
    }

    private void loadGlobalDrug(String drugId) {
        DrugInfoHelper helper = new DrugInfoHelper(this, db);
        helper.loadGlobalDrug(drugId, globalDrugScroll,
                sectionDescription, sectionDosage, sectionMealTiming, sectionPrecautions,
                chipGroupTags, chipGroupBrands, chipGroupSynonyms);
    }


}