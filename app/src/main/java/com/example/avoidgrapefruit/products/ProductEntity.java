package com.example.avoidgrapefruit.products;

import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductEntity implements Serializable, DisplayableItem {

    private String name;

    @PropertyName("image_url")
    private String imageUrl;

    private String category;
    private String subtype;
    private List<String> tags;

    @PropertyName("nutritional_components")
    private List<String> nutritionalComponents;

    private List<String> examples;

    @PropertyName("regional_dishes")
    private Map<String, List<String>> regionalDishes;

    // ===== No-arg constructor required by Firestore =====
    public ProductEntity() { }

    // ---- DisplayableItem overrides ----
    @Override
    public String getName() { return name; }

    @Override
    public String getCategory() { return category != null ? category : "Food"; }

    @Override
    public String getDescription() { return subtype != null ? subtype : "No description available."; }

    // ===== Getters and Setters =====
    public void setName(String name) { this.name = name; }

    @PropertyName("image_url")
    public String getImageUrl() { return imageUrl; }

    @PropertyName("image_url")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setCategory(String category) { this.category = category; }
    public String getSubtype() { return subtype; }
    public void setSubtype(String subtype) { this.subtype = subtype; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getExamples() { return examples; }
    public void setExamples(List<String> examples) { this.examples = examples; }

    // ===== Nutritional Components =====
    @PropertyName("nutritional_components")
    public List<String> getNutritionalComponents() { return nutritionalComponents; }

    @PropertyName("nutritional_components")
    public void setNutritionalComponents(List<String> nutritionalComponents) {
        this.nutritionalComponents = nutritionalComponents;
    }

    // ===== Regional Dishes =====
    @PropertyName("regional_dishes")
    public Map<String, List<String>> getRegionalDishes() { return regionalDishes; }

    @PropertyName("regional_dishes")
    @SuppressWarnings("unchecked")
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
