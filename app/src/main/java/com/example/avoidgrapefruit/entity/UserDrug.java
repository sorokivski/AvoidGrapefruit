package com.example.avoidgrapefruit.entity;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDrug {
    private String uuid;
    private String drugId;
    private String drugName;
    private Date startDate;
    private Date endDate;
    private String dosage;
    private int amountPerDay;
    private boolean active;
    public enum IntakeTime {
        MORNING,
        AFTERNOON,
        EVENING
    }

    private List<IntakeTime> intakeTimes;



}
