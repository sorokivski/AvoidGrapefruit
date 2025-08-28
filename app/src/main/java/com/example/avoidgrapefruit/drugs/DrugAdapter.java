package com.example.avoidgrapefruit.drugs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.entity.Drug;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.DrugViewHolder> {

    private List<Drug> drugList;
    private OnDrugClickListener listener;

    public interface OnDrugClickListener {
        void onDrugClick(Drug drug);
    }

    public DrugAdapter(List<Drug> drugList, OnDrugClickListener listener) {
        this.drugList = drugList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_drug, parent, false);
        return new DrugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrugViewHolder holder, int position) {
        Drug drug = drugList.get(position);
        holder.bind(drug, listener);
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView drugName, drugDosage, drugIntake;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            drugName = itemView.findViewById(R.id.drug_name);
            drugDosage = itemView.findViewById(R.id.drug_dosage);
            drugIntake = itemView.findViewById(R.id.drug_intake_times);
        }

        public void bind(Drug drug, OnDrugClickListener listener) {
            drugName.setText(drug.getName());
            drugDosage.setText(drug.getDosageInfo() != null ? drug.getDosageInfo().getStrength() : "No dosage info");
            drugIntake.setText(drug.getGenericName());

            itemView.setOnClickListener(v -> listener.onDrugClick(drug));
        }
    }
}
