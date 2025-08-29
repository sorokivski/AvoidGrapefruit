package com.example.avoidgrapefruit.drugs;

import android.content.Context;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.entity.Drug;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class DrugInfoHelper {

    private final Context context;
    private final FirebaseFirestore db;

    public DrugInfoHelper(Context context, FirebaseFirestore db) {
        this.context = context;
        this.db = db;
    }

    public void loadGlobalDrug(
            String drugId,
            ScrollView globalDrugScroll,
            View sectionDescription,
            View sectionDosage,
            View sectionMealTiming,
            View sectionPrecautions,
            ChipGroup chipGroupTags,
            ChipGroup chipGroupBrands,
            ChipGroup chipGroupSynonyms
    ) {
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
                        setupExpandableSection(sectionMealTiming, "Meal Timing", globalDrug.getMealTiming(), R.color.accent_green);
                    } else {
                        sectionMealTiming.setVisibility(View.GONE);
                    }

                    setupExpandableSection(sectionPrecautions, "Precautions", joinList(globalDrug.getPrecautions()), R.color.wood_brown);

                    populateChips(chipGroupTags, globalDrug.getTags(), R.style.ChipTag);
                    populateChips(chipGroupBrands, globalDrug.getBrandNames(), R.style.ChipBrand);
                    populateChips(chipGroupSynonyms, globalDrug.getSynonyms(), R.style.ChipSynonym);
                });
    }

    public void setupExpandableSection(View section, String title, String content, @ColorRes int color) {
        TextView titleView = section.findViewById(R.id.sectionTitle);
        TextView contentView = section.findViewById(R.id.sectionContent);

        titleView.setText(title);
        contentView.setText(content);
        contentView.setVisibility(View.GONE);

        titleView.setTextColor(ContextCompat.getColor(context, color));
        titleView.setOnClickListener(v -> {
            contentView.setVisibility(
                    contentView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });
    }

    public void populateChips(ChipGroup chipGroup, List<String> items, int chipStyle) {
        chipGroup.removeAllViews();

        if (items == null || items.isEmpty()) {
            chipGroup.setVisibility(View.GONE);
            return;
        }

        chipGroup.setVisibility(View.VISIBLE);
        for (String item : items) {
            ContextThemeWrapper wrapper = new ContextThemeWrapper(context, chipStyle);
            Chip chip = new Chip(wrapper);
            chip.setText(item);
            chip.setClickable(false);
            chip.setCheckable(false);
            chipGroup.addView(chip);
        }
    }

    public String joinList(List<String> items) {
        return items == null ? "" : "• " + String.join("\n• ", items);
    }

    public String formatDosage(Drug.DosageInfo dosage) {
        if (dosage == null) return "";
        return "Form: " + dosage.getForm() +
                "\nFrequency: " + dosage.getFrequency() +
                "\nStrength: " + dosage.getStrength() +
                "\nDuration: " + dosage.getDuration();
    }
}
