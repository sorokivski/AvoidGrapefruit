package com.example.avoidgrapefruit.entity;


import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String uid; // Firebase Auth UID
    private String username;
    private String userPicture;
    private int age;
    private Gender gender;
    private String email;
    private String medicalConditions;
    private String allergies;
    private Map<String, UserDrug> drugs;

    public enum Gender {
        MALE,
        FEMALE
    }
}


