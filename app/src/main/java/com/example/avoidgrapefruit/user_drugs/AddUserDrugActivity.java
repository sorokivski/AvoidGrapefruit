package com.example.avoidgrapefruit.user_drugs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class AddUserDrugActivity extends AppCompatActivity {

    private EditText etDrugName, etDosage, etAmountPerDay, etStartDate, etEndDate;
    private Switch switchActive;
    private CheckBox cbMorning, cbAfternoon, cbEvening;

    private FirebaseFirestore db;
    private AuthManager authManager;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_drug);

        authManager = AuthManager.getInstance(this);
        db = authManager.getFirestore();

        etDrugName = findViewById(R.id.etDrugName);
        etDosage = findViewById(R.id.etDosage);
        etAmountPerDay = findViewById(R.id.etAmountPerDay);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        switchActive = findViewById(R.id.switchActive);
        Button btnSave = findViewById(R.id.btnSaveDrug);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());


        cbMorning = findViewById(R.id.cbMorning);
        cbAfternoon = findViewById(R.id.cbAfternoon);
        cbEvening = findViewById(R.id.cbEvening);

        etStartDate.setOnClickListener(v -> pickDate(etStartDate));
        etEndDate.setOnClickListener(v -> pickDate(etEndDate));

        btnSave.setOnClickListener(v -> saveDrug());
    }

    private void pickDate(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    target.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void saveDrug() {
        String userId = authManager.getCurrentUserId();
        if (userId == null) {
            authManager.logout(this);
            return;
        }

        String name = etDrugName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        boolean active = switchActive.isChecked();

        int amountPerDay;
        try {
            amountPerDay = Integer.parseInt(etAmountPerDay.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount per day must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        Date startDate = parseDate(etStartDate.getText().toString().trim());
        Date endDate = parseDate(etEndDate.getText().toString().trim());

        if (name.isEmpty() || dosage.isEmpty() || startDate == null || endDate == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        List<UserDrug.IntakeTime> intakeTimes = new ArrayList<>();
        if (cbMorning.isChecked()) intakeTimes.add(UserDrug.IntakeTime.MORNING);
        if (cbAfternoon.isChecked()) intakeTimes.add(UserDrug.IntakeTime.AFTERNOON);
        if (cbEvening.isChecked()) intakeTimes.add(UserDrug.IntakeTime.EVENING);

        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("drugs")
                .document(); // Auto-generates a unique ID


        UserDrug drug = new UserDrug(
                docRef.getId(),
                docRef.getId(),
                name,
                startDate,
                endDate,
                dosage,
                amountPerDay,
                active,
                intakeTimes
        );

        docRef.set(drug)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Drug added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Date parseDate(String text) {
        try {
            return dateFormat.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}
