package com.example.avoidgrapefruit.user_drugs;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.Drug;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
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
        Log.wtf("User: ", userId);
        Log.wtf("DrugId: ", drugId);
        Log.wtf("User drug id:  ", uuid);


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
        db.collection("drugs")
                .document(drugId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Drug globalDrug = doc.toObject(Drug.class);
                    if (globalDrug == null) return;

                    globalDrugScroll.setVisibility(View.VISIBLE);

                    setupExpandableSection(sectionDescription, "Description", globalDrug.getDescription(), R.color.citrus_yellow);
                    setupExpandableSection(sectionDosage, "Dosage Info", formatDosage(globalDrug.getDosageInfo()), R.color.accent_green);
                    if (globalDrug.isMealTimingRequired()) {
                        setupExpandableSection(
                                sectionMealTiming,
                                "Meal Timing",
                                globalDrug.getMealTiming(),
                                R.color.accent_green
                        );
                    } else {
                        sectionMealTiming.setVisibility(View.GONE);
                    }

                    setupExpandableSection(sectionPrecautions, "Precautions", joinList(globalDrug.getPrecautions()), R.color.wood_brown);

                    populateChips(chipGroupTags, globalDrug.getTags(), R.style.ChipTag);
                    populateChips(chipGroupBrands, globalDrug.getBrandNames(), R.style.ChipBrand);
                    populateChips(chipGroupSynonyms, globalDrug.getSynonyms(), R.style.ChipSynonym);
                });
    }

    private void setupExpandableSection(View section, String title, String content, @ColorRes int color) {


        TextView titleView = section.findViewById(R.id.sectionTitle);
        TextView contentView = section.findViewById(R.id.sectionContent);

        titleView.setText(title);
        contentView.setText(content);
        contentView.setVisibility(View.GONE);

        // Tint title color
        titleView.setTextColor(ContextCompat.getColor(this, color));

        // Toggle expand/collapse
        titleView.setOnClickListener(v -> {
            contentView.setVisibility(contentView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
    }


    private void populateChips(ChipGroup chipGroup, List<String> items, int chipStyle) {
        chipGroup.removeAllViews();

        if (items == null || items.isEmpty()) {
            chipGroup.setVisibility(View.GONE);
            return;
        } else {
            chipGroup.setVisibility(View.VISIBLE);
        }

        for (String item : items) {
            ContextThemeWrapper wrapper = new ContextThemeWrapper(this, chipStyle);
            Chip chip = new Chip(wrapper);
            chip.setText(item);
            chip.setClickable(false);
            chip.setCheckable(false);
            chipGroup.addView(chip);
        }
    }



    private String joinList(List<String> items) {
        return items == null ? "" : "• " + String.join("\n• ", items);
    }

    private String formatDosage(Drug.DosageInfo dosage) {
        if (dosage == null) return "";
        return "Form: " + dosage.getForm() + "\nFrequency: " + dosage.getFrequency() +
                "\nStrength: " + dosage.getStrength() + "\nDuration: " + dosage.getDuration();
    }
}