package com.example.avoidgrapefruit;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.example.avoidgrapefruit.products.ProductEntity;
import com.example.avoidgrapefruit.products.DrugEntity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextInputEditText searchInput;
    private RecyclerView recyclerView;
    private HomeSearchAdapter adapter;

    private List<DisplayableItem> allProducts = new ArrayList<>();
    private List<DisplayableItem> filteredProducts = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        searchInput = view.findViewById(R.id.searchField);
        recyclerView = view.findViewById(R.id.searchResultsRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // After initializing recyclerView
        adapter = new HomeSearchAdapter(getContext(), filteredProducts, item -> {
            // Handle item click if needed
        });

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchProductsFromFirebase();

        // TextWatcher for TextInputEditText
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) { }
        });

        return view;
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
                // Initialize filtered list
                filteredProducts.clear();
                //filteredProducts.addAll(allProducts);
                //adapter.updateList(filteredProducts, searchInput.getText().toString());


            } else {
                Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        filteredProducts.clear();
        if (query == null || query.isEmpty()) {
            adapter.updateList(filteredProducts, "");
            filteredProducts.clear();
            return;
        }
        for (DisplayableItem item : allProducts) {
            boolean matchesQuery = item.getName().toLowerCase().contains(query.toLowerCase());

            if (item instanceof ProductEntity && ((ProductEntity) item).getTags() != null) {
                matchesQuery |= ((ProductEntity) item).getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()));
            } else if (item instanceof DrugEntity && ((DrugEntity) item).getTags() != null) {
                matchesQuery |= ((DrugEntity) item).getTags().stream()
                        .anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase()));
            }

            if (matchesQuery) filteredProducts.add(item);

        }
        adapter.updateList(filteredProducts, searchInput.getText().toString());

    }
}