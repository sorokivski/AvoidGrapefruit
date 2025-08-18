package com.example.avoidgrapefruit.products;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.avoidgrapefruit.R;

import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView nameText, categoryText, subtypeText, tagsText, nutritionText, examplesText, regionalText;
    private ImageView productImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        // Enable back arrow in the ActionBar
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
        productImage = findViewById(R.id.detailImage);

        ProductEntity product = (ProductEntity) getIntent().getSerializableExtra("product");

        if (product != null) {
            nameText.setText(product.getName());
            categoryText.setText("Category: " + product.getCategory());
            subtypeText.setText("Subtype: " + product.getSubtype());
            tagsText.setText("Tags: " + listToString(product.getTags()));
            nutritionText.setText("Nutrition: " + listToString(product.getNutritionalComponents()));
            examplesText.setText("Examples: " + listToString(product.getExamples()));
            regionalText.setText("Regional Dishes:\n" + mapToString(product.getRegionalDishes()));

            Glide.with(this).load(product.getImageUrl()).into(productImage);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private String listToString(List<String> list) {
        return list != null ? String.join(", ", list) : "None";
    }

    private String mapToString(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (String region : map.keySet()) {
            sb.append(region).append(": ").append(String.join(", ", map.get(region))).append("\n");
        }
        return sb.toString();
    }
}
