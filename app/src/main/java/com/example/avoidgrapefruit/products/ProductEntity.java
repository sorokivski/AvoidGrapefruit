package com.example.avoidgrapefruit.products;

import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.google.firebase.firestore.PropertyName;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
public class ProductEntity implements Serializable, DisplayableItem {
    private String name;
    private String imageUrl;
    private String category;
    private String subtype;
    private List<String> tags;

    @PropertyName("nutritional_components")
    private List<String> nutritionalComponents;

    private List<String> examples;

    @PropertyName("regional_dishes")
    private Map<String, List<String>> regionalDishes;

    // ---- DisplayableItem overrides ----
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return category != null ? category : "Food";
    }

    @Override
    public String getDescription() {
        return subtype != null ? subtype : "No description available.";
    }

    // Helper to safely set regionalDishes from Firestore snapshot
    @SuppressWarnings("unchecked")
    @PropertyName("regional_dishes")
    public void setRegionalDishes(Object map) {
        if (map instanceof Map) {
            Map<String, List<String>> safeMap = new HashMap<>();
            ((Map<String, Object>) map).forEach((key, value) -> {
                if (value instanceof List) {
                    safeMap.put(key, (List<String>) value);
                }
            });
            this.regionalDishes = safeMap;
        }
    }
}
