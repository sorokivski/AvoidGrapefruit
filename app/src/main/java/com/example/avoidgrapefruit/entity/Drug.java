package com.example.avoidgrapefruit.entity;


import com.google.firebase.firestore.PropertyName;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drug {
    private String id;
    private String name;
    private String genericName;


    private String description;
    private String mealTiming;
    private boolean mealTimingRequired;

    private List<String> brandNames;
    private List<String> synonyms;
    private List<String> tags;
    private List<String> precautions;
    private List<String> recommendations;

    private DosageInfo dosageInfo;

    // Nested class for dosage details
    @Data
    public static class DosageInfo {
        private String duration;
        private String form;
        private String frequency;
        private String strength;


    }

    @PropertyName("generic_name")
    public String getGenericName() {
        return genericName;
    }

    @PropertyName("generic_name")
    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    @PropertyName("meal_timing")
    public String getMealTiming() {
        return mealTiming;
    }

    @PropertyName("meal_timing")
    public void setMealTiming(String mealTiming) {
        this.mealTiming = mealTiming;
    }

    @PropertyName("meal_timing_required")
    public boolean isMealTimingRequired() {
        return mealTimingRequired;
    }

    @PropertyName("meal_timing_required")
    public void setMealTimingRequired(boolean mealTimingRequired) {
        this.mealTimingRequired = mealTimingRequired;
    }

    @PropertyName("brand_names")
    public List<String> getBrandNames() {
        return brandNames;
    }

    @PropertyName("brand_names")
    public void setBrandNames(List<String> brandNames) {
        this.brandNames = brandNames;
    }

    @PropertyName("dosage_info")
    public DosageInfo getDosageInfo() {
        return dosageInfo;
    }
    @PropertyName("dosage_info")
    public void setDosageInfo(DosageInfo dosageInfo) {
        this.dosageInfo = dosageInfo;
    }


}
