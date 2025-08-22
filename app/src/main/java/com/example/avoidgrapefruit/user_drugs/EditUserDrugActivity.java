package com.example.avoidgrapefruit.user_drugs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.AuthManager;
import com.example.avoidgrapefruit.entity.UserDrug;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class EditUserDrugActivity extends AppCompatActivity {

    private TextInputEditText etDrugName, etDosage, etAmountPerDay;
    private TextInputLayout layoutDrugName;
    private TextView tvStartDate, tvEndDate;
    private ChipGroup chipGroupIntakeTimes;
    private CheckBox checkActive;

    private UserDrug userDrug;
    private String userId;
    private String drugId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_drug);

        AuthManager authManager = AuthManager.getInstance(this);
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        drugId = getIntent().getStringExtra("drugId");

        if (drugId == null || drugId.isEmpty()) {
            Toast.makeText(this, "No drug ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        layoutDrugName = findViewById(R.id.layoutDrugName);
        etDrugName = findViewById(R.id.etDrugName);
        etDosage = findViewById(R.id.etDosage);
        etAmountPerDay = findViewById(R.id.etAmountPerDay);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        chipGroupIntakeTimes = findViewById(R.id.chipGroupIntakeTimes);
        checkActive = findViewById(R.id.checkActive);
        Button btnSave = findViewById(R.id.btnSaveDrug);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvStartDate.setOnClickListener(v -> showDatePicker(true));
        tvEndDate.setOnClickListener(v -> showDatePicker(false));


        btnSave.setOnClickListener(v -> saveDrug());

        loadDrugData();
    }

    private void loadDrugData() {
        db.collection("users")
                .document(userId)
                .collection("drugs")
                .document(drugId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Drug not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    userDrug = doc.toObject(UserDrug.class);
                    if (userDrug == null) {
                        Toast.makeText(this, "Failed to parse drug data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // Populate UI
                    if (userDrug.getUuid().equals(userDrug.getDrugId())) {
                        layoutDrugName.setVisibility(View.VISIBLE);
                        etDrugName.setText(userDrug.getDrugName());
                    }

                    etDosage.setText(userDrug.getDosage());
                    etAmountPerDay.setText(String.valueOf(userDrug.getAmountPerDay()));
                    checkActive.setChecked(userDrug.isActive());

                    tvStartDate.setText("Start Date: " + userDrug.getStartDate());
                    tvEndDate.setText("End Date: " + userDrug.getEndDate());

                    for (UserDrug.IntakeTime time : userDrug.getIntakeTimes()) {
                        if (time == UserDrug.IntakeTime.MORNING)
                            chipGroupIntakeTimes.check(R.id.chipMorning);
                        if (time == UserDrug.IntakeTime.AFTERNOON)
                            chipGroupIntakeTimes.check(R.id.chipAfternoon);
                        if (time == UserDrug.IntakeTime.EVENING)
                            chipGroupIntakeTimes.check(R.id.chipEvening);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading drug: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void saveDrug() {
        if (userDrug == null) return;

        if (layoutDrugName.getVisibility() == View.VISIBLE) {
            userDrug.setDrugName(etDrugName.getText().toString().trim());
        }

        userDrug.setDosage(etDosage.getText().toString().trim());

        try {
            userDrug.setAmountPerDay(Integer.parseInt(etAmountPerDay.getText().toString().trim()));
        } catch (Exception e) {
            userDrug.setAmountPerDay(1);
        }

        userDrug.setActive(checkActive.isChecked());

        List<UserDrug.IntakeTime> times = new ArrayList<>();
        if (((Chip) findViewById(R.id.chipMorning)).isChecked()) times.add(UserDrug.IntakeTime.MORNING);
        if (((Chip) findViewById(R.id.chipAfternoon)).isChecked()) times.add(UserDrug.IntakeTime.AFTERNOON);
        if (((Chip) findViewById(R.id.chipEvening)).isChecked()) times.add(UserDrug.IntakeTime.EVENING);
        userDrug.setIntakeTimes(times);

        db.collection("users")
                .document(userId)
                .collection("drugs")
                .document(userDrug.getUuid())
                .set(userDrug)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Drug updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void showDatePicker(boolean isStart) {
        final Calendar calendar = Calendar.getInstance();

        // Pre-fill with existing date if available
        Date currentDate = isStart ? userDrug.getStartDate() : userDrug.getEndDate();
        if (currentDate != null) {
            calendar.setTime(currentDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    Date pickedDate = calendar.getTime();

                    if (isStart) {
                        userDrug.setStartDate(pickedDate);
                        tvStartDate.setText("Start Date: " + pickedDate);
                    } else {
                        userDrug.setEndDate(pickedDate);
                        tvEndDate.setText("End Date: " + pickedDate);
                    }
                },
                year, month, day
        );

        datePickerDialog.show();
    }

}
