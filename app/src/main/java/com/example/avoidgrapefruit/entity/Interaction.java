package com.example.avoidgrapefruit.entity;

import com.google.firebase.firestore.PropertyName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interaction {

        private String description;
        private String drugId;
        private String drugName;
        private String management;
        private String mechanism;
        private String product;
        private String productType;
        private String severity;
    @PropertyName("drug_id")
    public String getDrugId() {
        return drugId;
    }

    @PropertyName("drug_id")
    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    @PropertyName("drug_name")
    public String getDrugName() {
        return drugName;
    }

    @PropertyName("drug_name")
    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    @PropertyName("product_type")
    public String getProductType() {
        return productType;
    }
    @PropertyName("product_type")
    public void setProductType(String productType) {
        this.productType = productType;
    }


}
