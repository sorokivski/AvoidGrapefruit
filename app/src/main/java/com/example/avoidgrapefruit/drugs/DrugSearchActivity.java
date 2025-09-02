package com.example.avoidgrapefruit.drugs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.entity.Drug;
import com.example.avoidgrapefruit.user_drugs.AddUserDrugActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DrugSearchActivity extends AppCompatActivity {

    private LinearLayout layoutNoResults;
    private TextView tvNoResultsMessage;
    private Button btnAddPersonalDrug;
    private DrugAdapter adapter;
    private List<Drug> drugList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_search);

        db = FirebaseFirestore.getInstance();

        EditText etSearch = findViewById(R.id.etSearch);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        RecyclerView recyclerView = findViewById(R.id.recyclerDrugs);

        layoutNoResults = findViewById(R.id.layoutNoResults);
        tvNoResultsMessage = findViewById(R.id.tvNoResultsMessage);
        btnAddPersonalDrug = findViewById(R.id.btnAddPersonalDrug);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DrugAdapter(drugList, this::onDrugSelected);
        recyclerView.setAdapter(adapter);
        etSearch.addTextChangedListener(new TextWatcher() {
            private final Handler handler = new Handler(Looper.getMainLooper());
            private Runnable workRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel pending tasks
                if (workRunnable != null) {
                    handler.removeCallbacks(workRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();

                workRunnable = () -> {
                    if (query.length() >= 3) {
                        searchDrugs(query);
                    } else {
                        drugList.clear();
                        adapter.notifyDataSetChanged();
                    }
                };

                // Run after 400ms idle
                handler.postDelayed(workRunnable, 400);
            }
        });


    }

    private void searchDrugs(String query) {
        db.collection("drugs")
                .orderBy("name_lower")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snap -> {
                    drugList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Drug drug = doc.toObject(Drug.class);
                        if (drug != null) {
                            drugList.add(drug);
                        }
                    }

                    if (drugList.isEmpty()) {
                        fallbackSearch(query);
                    } else {
                        updateUI(query);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fallbackSearch(String query) {
        db.collection("drugs")
                .whereArrayContains("synonyms_lower", query)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Drug drug = doc.toObject(Drug.class);
                        if (drug != null && !drugList.contains(drug)) {
                            drugList.add(drug);
                        }
                    }
                    updateUI(query);
                });

        db.collection("drugs")
                .whereArrayContains("brand_names_lower", query)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Drug drug = doc.toObject(Drug.class);
                        if (drug != null && !drugList.contains(drug)) {
                            drugList.add(drug);
                        }
                    }
                    updateUI(query);
                });

        db.collection("drugs")
                .whereEqualTo("generic_name_lower", query)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Drug drug = doc.toObject(Drug.class);
                        if (drug != null && !drugList.contains(drug)) {
                            drugList.add(drug);
                        }
                    }
                    updateUI(query);
                });
    }

    private void updateUI(String query) {
        adapter.notifyDataSetChanged();

        if (drugList.isEmpty()) {
            layoutNoResults.setVisibility(View.VISIBLE);
            tvNoResultsMessage.setText(
                    "We haven't found anything for \"" + query + "\".\n" +
                            "You can add this as a personal drug by clicking below."
            );

            btnAddPersonalDrug.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddUserDrugActivity.class);
                intent.putExtra("prefill_name", query);
                startActivity(intent);
            });
        } else {
            layoutNoResults.setVisibility(View.GONE);
            btnAddPersonalDrug.setOnClickListener(null);
        }
    }


    private void onDrugSelected(Drug drug) {
        Intent intent = new Intent(this, DrugInfoActivity.class);
        intent.putExtra("drugId", drug.getId());
        Log.wtf("drug id: ", drug.getId());
        startActivity(intent);
    }
}
