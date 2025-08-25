package com.example.avoidgrapefruit.interactions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InteractionsActivity extends AppCompatActivity {

    private LinearLayout containerInteractions;
    private TextView tvEmpty;

    private FirebaseFirestore firestore;
    private String userId;

    private int totalDrugs = 0;
    private int completedFetches = 0;
    private boolean foundAnyInteractions = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactions);

        containerInteractions = findViewById(R.id.containerInteractions);
        tvEmpty = findViewById(R.id.tvEmptyInteractions);

        AuthManager authManager = AuthManager.getInstance(this);
        firestore = authManager.getFirestore();
        userId = authManager.getCurrentUserId();

        if (userId == null) {
            showEmpty("⚠ You need to log in to see possible interactions.");
            return;
        }

        loadUserInteractions();
    }

    private void loadUserInteractions() {
        firestore.collection("users")
                .document(userId)
                .collection("drugs")
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        showEmpty("You don’t have any drugs in your list yet.");
                        return;
                    }

                    totalDrugs = query.size();
                    completedFetches = 0;
                    foundAnyInteractions = false;

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String drugId = doc.getId();
                        fetchInteractionsForDrug(drugId);
                    }
                })
                .addOnFailureListener(e -> showEmpty("⚠ Could not load your drug list."));
    }
//TODO fix th dmn
    private void fetchInteractionsForDrug(String drugId) {
        String lowerDrugId = drugId.toLowerCase();
        String start =lowerDrugId + "___";
        String end = lowerDrugId + "___\uf8ff";

        firestore.collection("interactions")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), start)
                .whereLessThan(FieldPath.documentId(), end)
                .get()
                .addOnSuccessListener(query -> {
                    completedFetches++;

                    if (!query.isEmpty()) {
                        foundAnyInteractions = true;
                        Map<String, List<String>> merged = new HashMap<>();

                        for (DocumentSnapshot snapshot : query.getDocuments()) {
                            Map<String, Object> data = snapshot.getData();
                            if (data == null) continue;

                            // merge each severity bucket
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                String severity = entry.getKey();
                                List<String> products = (List<String>) entry.getValue();
                                if (products == null) continue;

                                merged.computeIfAbsent(severity, k -> new ArrayList<>()).addAll(products);
                            }
                        }

                        if (!merged.isEmpty()) {
                            showInteractions(merged);
                        }
                    }

                    checkIfDone();
                })
                .addOnFailureListener(e -> {
                    completedFetches++;
                    checkIfDone();
                });
    }


    private void showInteractions(Map<String, List<String>> interactionsBySeverity) {
        if (!foundAnyInteractions) {
            containerInteractions.removeAllViews(); // clear once before first interaction
        }

        for (Map.Entry<String, List<String>> entry : interactionsBySeverity.entrySet()) {
            String severity = entry.getKey();
            List<String> products = entry.getValue();

            if (products == null || products.isEmpty()) continue;

            View groupView = LayoutInflater.from(this).inflate(R.layout.item_interaction_group, containerInteractions, false);

            TextView tvHeader = groupView.findViewById(R.id.tvSeverityHeader);
            LinearLayout llItems = groupView.findViewById(R.id.llProducts);

            tvHeader.setText("Severity: " + severity);

            for (String product : products) {
                TextView tvProduct = new TextView(this);
                tvProduct.setText("• " + product);
                tvProduct.setTextSize(14f);
                tvProduct.setPadding(24, 8, 24, 8);
                llItems.addView(tvProduct);
            }

            containerInteractions.addView(groupView);
        }

        tvEmpty.setVisibility(View.GONE);
    }

    private void checkIfDone() {
        if (completedFetches >= totalDrugs && !foundAnyInteractions) {
            showEmpty("We checked your drug list and found no products with known interactions.");
        }
    }

//TODO add gps based to regional dishes

    private void showEmpty(String msg) {
        containerInteractions.removeAllViews();
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
