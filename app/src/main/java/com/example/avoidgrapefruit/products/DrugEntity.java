package com.example.avoidgrapefruit.products;

import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.google.firebase.firestore.PropertyName;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class DrugEntity implements Serializable, DisplayableItem {

    private String id;
    private String name;

    @PropertyName("generic_name")
    private String genericName;

    @PropertyName("brand_names")
    private List<String> brandNames;

    private String description;

    private List<String> precautions;
    private List<String> tags;

    @PropertyName("meal_timing_required")
    private Boolean mealTimingRequired;

    @PropertyName("meal_timing")
    private Map<String, String> mealTiming;

    private List<String> recommendations;

    @PropertyName("dosage_info")
    private DosageInfo dosageInfo;

    private String source;

    // DisplayableItem interface
    @Override
    public String getName() { return name; }

    @Override
    public String getCategory() { return "Drug"; }

    @Override
    public String getDescription() {
        if (description != null && !description.isEmpty()) return description;
        if (dosageInfo != null && dosageInfo.getForm() != null) return "Form: " + dosageInfo.getForm();
        return "No description available.";
    }

    @Data
    public static class DosageInfo implements Serializable {
        private String form;
        private String strength;
        private String frequency;
        private String duration;
    }
}
