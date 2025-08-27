package com.example.avoidgrapefruit.interactions;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.Interaction;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class InteractionsActivity extends AppCompatActivity {

    private LinearLayout containerInteractions;
    private TextView tvEmpty;

    private FirebaseFirestore firestore;
    private String userId;

    private int totalDrugs = 0;
    private int completedFetches = 0;
    private boolean foundAnyInteractions = false;
    private FusedLocationProviderClient fusedLocationClient;
    private String userRegion = null;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;


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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fetchUserRegion(() -> loadUserInteractions());
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

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
                        String drugId = doc.getString("drugId");
                        fetchInteractionsForDrug(drugId);
                    }
                })
                .addOnFailureListener(e -> showEmpty("⚠ Could not load your drug list."));
    }
    private void fetchInteractionsForDrug(String drugId) {
        String start = drugId + "__";
        String end = drugId + "__\uf8ff";

        firestore.collection("interactions")
                .whereGreaterThanOrEqualTo(FieldPath.documentId(), start)
                .whereLessThan(FieldPath.documentId(), end)
                .get()
                .addOnSuccessListener(query -> {
                    completedFetches++;

                    if (!query.isEmpty()) {
                        foundAnyInteractions = true;

                        Map<String, List<Interaction>> grouped = new HashMap<>();
                        String drugDisplayName = null;

                        for (DocumentSnapshot snapshot : query.getDocuments()) {
                            Interaction interaction = snapshot.toObject(Interaction.class);
                            if (interaction == null || interaction.getSeverity() == null) continue;

                            if (drugDisplayName == null && interaction.getDrugName() != null) {
                                drugDisplayName = interaction.getDrugName();
                            }
                            grouped.computeIfAbsent(interaction.getSeverity(), k -> new ArrayList<>())
                                    .add(interaction);
                        }

                        if (!grouped.isEmpty()) {
                            if (drugDisplayName == null) drugDisplayName = "Drug";
                            showInteractions(drugDisplayName, grouped);
                        }
                    }

                    checkIfDone();
                })
                .addOnFailureListener(e -> {
                    completedFetches++;
                    checkIfDone();
                });
    }

    private void showInteractions(String drugName, Map<String, List<Interaction>> interactionsBySeverity) {
        if (!foundAnyInteractions) {
            containerInteractions.removeAllViews(); // clear once before the first render
        }

        TextView drugHeader = new TextView(this);
        drugHeader.setText(drugName);
        drugHeader.setTextSize(18f);
        drugHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        drugHeader.setPadding(8, 16, 8, 8);
        containerInteractions.addView(drugHeader);

        for (Map.Entry<String, List<Interaction>> entry : interactionsBySeverity.entrySet()) {
            String severity = entry.getKey();
            List<Interaction> interactions = entry.getValue();
            if (interactions == null || interactions.isEmpty()) continue;

            View groupView = LayoutInflater.from(this)
                    .inflate(R.layout.item_interaction_group, containerInteractions, false);

            TextView tvHeader = groupView.findViewById(R.id.tvSeverityHeader);
            LinearLayout llItems = groupView.findViewById(R.id.llProducts);
            tvHeader.setText("Severity: " + severity);

            for (Interaction it : interactions) {
                View row = LayoutInflater.from(this)
                        .inflate(R.layout.item_interaction, llItems, false);

                TextView tvProduct = row.findViewById(R.id.tvProduct);
                TextView tvDescription = row.findViewById(R.id.tvDescription);
                TextView tvManagement = row.findViewById(R.id.tvManagement);

                tvProduct.setText("• " + (it.getProduct() != null ? it.getProduct() : "product"));
                tvDescription.setText(it.getDescription() != null ? it.getDescription() : "");
                tvManagement.setText(it.getManagement() != null ? ("Advice: " + it.getManagement()) : "");

                // expand/collapse details
                row.setOnClickListener(v -> {
                    int vis = (tvDescription.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
                    tvDescription.setVisibility(vis);
                    tvManagement.setVisibility(vis);
                });

                llItems.addView(row);
            }

            containerInteractions.addView(groupView);
        }

        if (userRegion != null) {

            String mappedRegion = RegionMapper.resolve(userRegion);

            Set<String> productNames = new HashSet<>();
            for (List<Interaction> list : interactionsBySeverity.values()) {
                for (Interaction it : list) {
                    if (it.getProduct() != null) {
                        productNames.add(it.getProduct());
                    }
                }
            }

            if (!productNames.isEmpty()) {
                firestore.collection("products")
                        .whereIn(FieldPath.documentId(), new ArrayList<>(productNames))
                        .get()
                        .addOnSuccessListener(query -> {
                            for (DocumentSnapshot doc : query.getDocuments()) {
                                String productName = doc.getId();
                                List<String> examples = (List<String>) doc.get("examples");
                                Map<String, Object> regionalMap = (Map<String, Object>) doc.get("regional_dishes");
                                //dishes
                                List<String> regionalDishes = new ArrayList<>();
                                if (regionalMap != null) {
                                    if (regionalMap.containsKey(userRegion)) {
                                        regionalDishes.addAll((List<String>) regionalMap.get(userRegion));
                                    }

                                    if (!mappedRegion.equals(userRegion) && regionalMap.containsKey(mappedRegion)) {
                                        regionalDishes.addAll((List<String>) regionalMap.get(mappedRegion));
                                    }
                                }

                                if (!regionalDishes.isEmpty() || (examples != null && !examples.isEmpty())) {
                                    View regionBlock = LayoutInflater.from(this)
                                            .inflate(R.layout.item_interaction_group, containerInteractions, false);

                                    TextView tvHeader = regionBlock.findViewById(R.id.tvSeverityHeader);
                                    LinearLayout llProducts = regionBlock.findViewById(R.id.llProducts);

                                    tvHeader.setText("⚠ Regional Risk: " + productName + " in " + userRegion);

                                    if (!regionalDishes.isEmpty()) {
                                        TextView dishHeader = new TextView(this);
                                        dishHeader.setText("Regional Dishes:");
                                        dishHeader.setTypeface(null, Typeface.BOLD);
                                        dishHeader.setTextSize(15f);
                                        dishHeader.setPadding(0, 12, 0, 4);
                                        llProducts.addView(dishHeader);

                                        for (String dish : regionalDishes) {
                                            TextView tvDish = new TextView(this);
                                            tvDish.setText("• " + dish);
                                            tvDish.setTextSize(14f);
                                            tvDish.setPadding(12, 4, 12, 4);
                                            llProducts.addView(tvDish);
                                        }
                                    }

                                   //examples
                                    if (examples != null && !examples.isEmpty()) {
                                        TextView tagHeader = new TextView(this);
                                        tagHeader.setText("Examples:");
                                        tagHeader.setTypeface(null, Typeface.BOLD);
                                        tagHeader.setTextSize(15f);
                                        tagHeader.setPadding(0, 12, 0, 4);
                                        llProducts.addView(tagHeader);

                                        ChipGroup chipGroup = new ChipGroup(this);
                                        chipGroup.setChipSpacingHorizontal(8);
                                        chipGroup.setChipSpacingVertical(8);
                                        chipGroup.setSingleLine(false);
                                        chipGroup.setSingleSelection(false);

                                        ContextThemeWrapper wrapper = new ContextThemeWrapper(this, R.style.ChipSynonym);
                                        for (String example : examples) {
                                            Chip chip = new Chip(wrapper);
                                            chip.setText(example);
                                            chip.setClickable(false);
                                            chip.setCheckable(false);
                                            chipGroup.addView(chip);
                                        }

                                        llProducts.addView(chipGroup);
                                    }

                                    containerInteractions.addView(regionBlock);
                                }
                            }
                        });
            }
        }




        tvEmpty.setVisibility(View.GONE);
    }

    private void checkIfDone() {
        if (completedFetches >= totalDrugs && !foundAnyInteractions) {
            showEmpty("We checked your drug list and found no products with known interactions.");
        }
    }

    private void showEmpty(String msg) {
        containerInteractions.removeAllViews();
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserRegion(this::loadUserInteractions);

            } else {
                Toast.makeText(this, "Location permission is required to detect your region.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchUserRegion(Runnable onRegionReady) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1,
                        new Geocoder.GeocodeListener() {
                            @Override
                            public void onGeocode(List<Address> addresses) {
                                if (addresses != null && !addresses.isEmpty()) {
                                    userRegion = addresses.get(0).getCountryName();
                                    Log.wtf("Region", "Detected region: " + userRegion);
                                }
                                if (onRegionReady != null) onRegionReady.run();
                            }

                            @Override
                            public void onError(@NonNull String errorMessage) {
                                Log.e("Region", "Geocoding failed: " + errorMessage);
                                if (onRegionReady != null) onRegionReady.run(); // still run even if failed
                            }
                        }
                );
            } else {
                if (onRegionReady != null) onRegionReady.run(); // fallback if location is null
            }
        });
    }



}
