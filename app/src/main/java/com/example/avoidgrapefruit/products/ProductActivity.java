package com.example.avoidgrapefruit.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.home.BaseActivity;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends BaseActivity {

    private ProductAdapter adapter;
    private AutoCompleteTextView searchInput;
    private Spinner categorySpinner;

    private  List<ProductEntity> allProducts = new ArrayList<>();
    private List<ProductEntity> filteredProducts = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.product;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Products");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        categorySpinner = findViewById(R.id.categorySpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, filteredProducts);
        recyclerView.setAdapter(adapter);


        AuthManager authManager = AuthManager.getInstance(this);
        db = authManager.getFirestore();

        fetchProductsFromFirebase();
        setupCategoryFilter();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
                setupSearch();
            } else {
                Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        String[] itemNames = allProducts.stream()
                .map(DisplayableItem::getName)
                .toArray(String[]::new);

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                itemNames
        );
        searchInput.setAdapter(searchAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString(), categorySpinner.getSelectedItem().toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryFilter() {
        categorySpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"All", "Food", "Supplement", "Other"}
        ));

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterProducts(searchInput.getText().toString(), categorySpinner.getSelectedItem().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filterProducts(String query, String category) {
        filteredProducts.clear();
        for (ProductEntity product : allProducts) {
            boolean matchesQuery = product.getName().toLowerCase().contains(query.toLowerCase());

            if (product.getTags() != null) {
                matchesQuery |= product.getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()));
            }

            boolean matchesCategory = category.equals("All") ||
                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase(category));

            if (matchesQuery && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        adapter.updateList(filteredProducts);
    }
}
