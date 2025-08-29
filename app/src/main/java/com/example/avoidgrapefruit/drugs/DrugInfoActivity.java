package com.example.avoidgrapefruit.drugs;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.Drug;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.example.avoidgrapefruit.user_drugs.EditUserDrugActivity;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class DrugInfoActivity extends AppCompatActivity {

    private TextView tvDrugName;
    private View sectionDescription, sectionDosage, sectionPrecautions, sectionMealTiming;
    private ChipGroup chipGroupTags, chipGroupBrands, chipGroupSynonyms;
    private ScrollView globalDrugScroll;
    private Button btnAddToMyDrugs;

    private FirebaseFirestore db;
    private DrugInfoHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drug_info);

        AuthManager authManager = AuthManager.getInstance(this);
        db = authManager.getFirestore();

        String drugId = getIntent().getStringExtra("drugId");
        if (drugId == null) {
            Toast.makeText(this, "Missing drug ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        helper = new DrugInfoHelper(this, db);
        helper.loadGlobalDrug(drugId,
                globalDrugScroll,
                sectionDescription, sectionDosage, sectionMealTiming, sectionPrecautions,
                chipGroupTags, chipGroupBrands, chipGroupSynonyms);

        loadGlobalDrug(drugId);
    }


    private void initViews() {
        tvDrugName = findViewById(R.id.tvDrugName);
        globalDrugScroll = findViewById(R.id.globalDrugScroll);

        sectionDescription = findViewById(R.id.sectionDescription);
        sectionDosage = findViewById(R.id.sectionDosage);
        sectionPrecautions = findViewById(R.id.sectionPrecautions);
        sectionMealTiming = findViewById(R.id.sectionMealTiming);

        chipGroupTags = findViewById(R.id.chipGroupTags);
        chipGroupBrands = findViewById(R.id.chipGroupBrands);
        chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);

        btnAddToMyDrugs = findViewById(R.id.btnAddToMyDrugs);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadGlobalDrug(String drugId) {
        db.collection("drugs")
                .document(drugId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Drug drug = doc.toObject(Drug.class);
                    if (drug == null) return;

                    tvDrugName.setText(drug.getName());

                    helper.setupExpandableSection(sectionDescription, "Description", drug.getDescription(), R.color.citrus_yellow);
                    helper.setupExpandableSection(sectionDosage, "Dosage Info", helper.formatDosage(drug.getDosageInfo()), R.color.accent_green);

                    if (drug.isMealTimingRequired()) {
                        helper.setupExpandableSection(sectionMealTiming, "Meal Timing", drug.getMealTiming(), R.color.accent_green);
                    } else {
                        sectionMealTiming.setVisibility(View.GONE);
                    }

                    helper.setupExpandableSection(sectionPrecautions, "Precautions", helper.joinList(drug.getPrecautions()), R.color.wood_brown);

                    helper.populateChips(chipGroupTags, drug.getTags(), R.style.ChipTag);
                    helper.populateChips(chipGroupBrands, drug.getBrandNames(), R.style.ChipBrand);
                    helper.populateChips(chipGroupSynonyms, drug.getSynonyms(), R.style.ChipSynonym);

                    btnAddToMyDrugs.setOnClickListener(v -> addUserDrug(drug));
                });
    }

    private void addUserDrug(Drug drug) {
        String userId = AuthManager.getInstance(this).getCurrentUserId();
        if (userId == null) return;

        DocumentReference userDrugRef = db.collection("users")
                .document(userId)
                .collection("drugs")
                .document();

        UserDrug userDrug = new UserDrug(
                userDrugRef.getId(),
                drug.getId(),
                drug.getName(),
                new Date(),
                new Date(),
                "",
                1,
                true,
                new ArrayList<>()
        );

        userDrugRef.set(userDrug)
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(this, EditUserDrugActivity.class);
                    intent.putExtra("drugId", userDrug.getUuid());
                    startActivity(intent);
                    finish();
                });
    }
}
