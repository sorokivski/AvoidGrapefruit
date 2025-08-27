package com.example.avoidgrapefruit.interfaces;

import java.io.Serializable;

public interface DisplayableItem extends Serializable {
    String getName();       // e.g., drugName or productName
    String getCategory();    // e.g., "Drug" or "Product"
    String getDescription(); // optional
}

