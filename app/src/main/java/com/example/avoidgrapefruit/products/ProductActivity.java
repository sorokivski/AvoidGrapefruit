package com.example.avoidgrapefruit.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private AutoCompleteTextView searchInput;
    private Spinner categorySpinner;

    private List<ProductEntity> allProducts = new ArrayList<>();
    private List<ProductEntity> filteredProducts = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        categorySpinner = findViewById(R.id.categorySpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, filteredProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchProductsFromFirebase();
        setupCategoryFilter();
    }

    private void fetchProductsFromFirebase() {
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allProducts.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    ProductEntity product = doc.toObject(ProductEntity.class);
                    allProducts.add(product);
                }
                filteredProducts.clear();
                filteredProducts.addAll(allProducts);
                adapter.updateList(filteredProducts);
                setupSearch(); // Refresh search suggestions after loading
            } else {
                Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        String[] productNames = allProducts.stream()
                .map(ProductEntity::getName)
                .toArray(String[]::new);

        searchInput.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, productNames));

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString(), categorySpinner.getSelectedItem().toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        categorySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Food", "Drug"}));

        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterProducts(searchInput.getText().toString(), categorySpinner.getSelectedItem().toString());
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void filterProducts(String query, String category) {
        filteredProducts.clear();
        for (ProductEntity p : allProducts) {
            boolean matchesQuery = p.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (p.getTags() != null && p.getTags().stream()
                            .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase())));
            boolean matchesCategory = category.equals("All") || p.getCategory().equalsIgnoreCase(category);
            if (matchesQuery && matchesCategory) {
                filteredProducts.add(p);
            }
        }
        adapter.updateList(filteredProducts);
    }
}
