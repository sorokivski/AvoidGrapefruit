package com.example.avoidgrapefruit.products;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private AutoCompleteTextView searchInput;
    private Spinner categorySpinner;

    private final List<DisplayableItem> allProducts = new ArrayList<>();
    private final List<DisplayableItem> filteredProducts = new ArrayList<>();

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product, container, false);

        recyclerView = root.findViewById(R.id.recyclerView);
        searchInput = root.findViewById(R.id.searchInput);
        categorySpinner = root.findViewById(R.id.categorySpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(getContext(), filteredProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchProductsFromFirebase();
        setupCategoryFilter();

        return root;
    }

    private void fetchProductsFromFirebase() {
        db.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allProducts.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String category = doc.getString("category");
                    if (category != null && category.equalsIgnoreCase("Drug")) {
                        DrugEntity drug = doc.toObject(DrugEntity.class);
                        allProducts.add(drug);
                    } else {
                        ProductEntity product = doc.toObject(ProductEntity.class);
                        allProducts.add(product);
                    }
                }
                filteredProducts.clear();
                filteredProducts.addAll(allProducts);
                adapter.updateList(filteredProducts);
                setupSearch();
            } else {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        String[] itemNames = allProducts.stream()
                .map(DisplayableItem::getName)
                .toArray(String[]::new);

        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, itemNames);

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
        categorySpinner.setAdapter(new ArrayAdapter<>(requireContext(),
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
        for (DisplayableItem item : allProducts) {
            boolean matchesQuery = item.getName().toLowerCase().contains(query.toLowerCase());

            if (item instanceof ProductEntity && ((ProductEntity)item).getTags() != null) {
                matchesQuery |= ((ProductEntity)item).getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()));
            } else if (item instanceof DrugEntity && ((DrugEntity)item).getTags() != null) {
                matchesQuery |= ((DrugEntity)item).getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()));
            }

            boolean matchesCategory = category.equals("All") || item.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) filteredProducts.add(item);
        }
        adapter.updateList(filteredProducts);
    }
}
