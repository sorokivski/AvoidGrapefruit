package com.example.avoidgrapefruit.products;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProductEntity  implements java.io.Serializable {
    private String name;
    private String imageUrl;
    private String category;
    private String subtype;
    private List<String> tags;
    private List<String> nutritionalComponents;
    private List<String> examples;
    private Map<String, List<String>> regionalDishes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getNutritionalComponents() {
        return nutritionalComponents;
    }

    public void setNutritionalComponents(List<String> nutritionalComponents) {
        this.nutritionalComponents = nutritionalComponents;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public Map<String, List<String>> getRegionalDishes() {
        return regionalDishes;
    }

    public void setRegionalDishes(Map<String, List<String>> regionalDishes) {
        this.regionalDishes = regionalDishes;
    }
}
