package com.example.avoidgrapefruit.products;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;

import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView nameText, categoryText, subtypeText, tagsText, nutritionText,
            examplesText, regionalText, extraInfoText;
    private ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Product Details");
        }

        nameText = findViewById(R.id.detailName);
        categoryText = findViewById(R.id.detailCategory);
        subtypeText = findViewById(R.id.detailSubtype);
        tagsText = findViewById(R.id.detailTags);
        nutritionText = findViewById(R.id.detailNutrition);
        examplesText = findViewById(R.id.detailExamples);
        regionalText = findViewById(R.id.detailRegional);
        extraInfoText = findViewById(R.id.detailExtraInfo); // New TextView for drug info
        productImage = findViewById(R.id.detailImage);

        DisplayableItem item = (DisplayableItem) getIntent().getSerializableExtra("item");

        if (item instanceof ProductEntity) {
            ProductEntity product = (ProductEntity) item;

            nameText.setText(product.getName());
            categoryText.setText("Category: " + product.getCategory());
            subtypeText.setText("Subtype: " + product.getSubtype());
            tagsText.setText("Tags: " + listToString(product.getTags()));
            nutritionText.setText("Nutrition: " + listToString(product.getNutritionalComponents()));
            examplesText.setText("Examples: " + listToString(product.getExamples()));
            regionalText.setText("Regional Dishes:\n" + mapToString(product.getRegionalDishes()));
            extraInfoText.setText(""); // No extra info for food

            Glide.with(this).load(product.getImageUrl()).into(productImage);

        } else if (item instanceof DrugEntity) {
            DrugEntity drug = (DrugEntity) item;

            nameText.setText(drug.getName());
            categoryText.setText("Category: " + drug.getCategory());
            subtypeText.setText("Generic Name: " + drug.getGenericName());
            tagsText.setText("Tags: " + listToString(drug.getTags()));

            // Dosage info
            if (drug.getDosageInfo() != null) {
                DrugEntity.DosageInfo d = drug.getDosageInfo();
                nutritionText.setText("Form: " + d.getForm() + "\nStrength: " + d.getStrength() +
                        "\nFrequency: " + d.getFrequency() + "\nDuration: " + d.getDuration());
            } else {
                nutritionText.setText("Dosage info: N/A");
            }

            // Precautions and recommendations
            StringBuilder extraInfo = new StringBuilder();
            if (drug.getPrecautions() != null && !drug.getPrecautions().isEmpty()) {
                extraInfo.append("Precautions:\n").append(listToString(drug.getPrecautions())).append("\n\n");
            }
            if (drug.getRecommendations() != null && !drug.getRecommendations().isEmpty()) {
                extraInfo.append("Recommendations:\n").append(listToString(drug.getRecommendations())).append("\n\n");
            }
            if (drug.getBrandNames() != null && !drug.getBrandNames().isEmpty()) {
                extraInfo.append("Brand Names:\n").append(listToString(drug.getBrandNames())).append("\n\n");
            }
            if (drug.getMealTiming() != null && !drug.getMealTiming().isEmpty()) {
                extraInfo.append("Meal Timing:\n");
                for (Map.Entry<String, String> entry : drug.getMealTiming().entrySet()) {
                    extraInfo.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
            }
            extraInfoText.setText(extraInfo.toString());

            examplesText.setText(""); // Not applicable
            regionalText.setText(""); // Not applicable
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String listToString(List<String> list) {
        return list != null && !list.isEmpty() ? String.join(", ", list) : "None";
    }

    private String mapToString(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            sb.append(key).append(": ").append(String.join(", ", map.get(key))).append("\n");
        }
        return sb.toString();
    }
}
