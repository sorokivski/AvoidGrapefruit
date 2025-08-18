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
//    public List<DrugIntake> generateSchedule() {
//        List<DrugIntake> schedule = new ArrayList<>();
//        LocalDate current = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//        LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//
//        while (!current.isAfter(end)) {
//            for (IntakeTime time : intakeTimes) {
//                schedule.add(new DrugIntake(drugId, current, time, false, null));
//            }
//            current = current.plusDays(1);
//        }
//        return schedule;
//    }


}
