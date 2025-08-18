package com.example.avoidgrapefruit.user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.entity.UserDrug;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDrugAdapter extends RecyclerView.Adapter<UserDrugAdapter.DrugViewHolder> {

    public interface OnDrugClickListener {
        void onCardClick(UserDrug drug);

        void onNameClick(UserDrug drug);

        void onEditClick(UserDrug drug);

        void onDeleteClick(UserDrug drug);
    }

    private List<UserDrug> drugs = new ArrayList<>();
    private final OnDrugClickListener listener;

    public UserDrugAdapter(OnDrugClickListener listener) {
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
        UserDrug drug = drugs.get(position);
        holder.bind(drug, listener);
    }

    @Override
    public int getItemCount() {
        return drugs.size();
    }

    public void setData(List<UserDrug> newData) {
        this.drugs = newData;
        notifyDataSetChanged();
    }

    public void removeDrug(UserDrug drug) {
        int index = drugs.indexOf(drug);
        if (index != -1) {
            drugs.remove(index);
            notifyItemRemoved(index);
        }
    }

    static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrugName, tvDosage, tvDates;
        ImageButton btnMenu;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(R.id.drug_name);
            tvDosage = itemView.findViewById(R.id.drug_dosage);
            tvDates = itemView.findViewById(R.id.drug_intake_times);
            btnMenu = itemView.findViewById(R.id.btnMoreOptions);

        }

        public void bind(UserDrug drug, OnDrugClickListener listener) {
            tvDrugName.setText(drug.getDrugName());
            tvDosage.setText(drug.getDosage() + " - " + drug.getAmountPerDay() + "x/day");
            tvDates.setText(formatDate(drug.getStartDate()) + " → " + formatDate(drug.getEndDate()));

            // Card click
            itemView.setOnClickListener(v -> listener.onCardClick(drug));

            // Drug name click
            tvDrugName.setOnClickListener(v -> listener.onNameClick(drug));

            // Menu click (edit / delete)
//            btnMenu.setOnClickListener(v -> {
//                PopupMenu menu = new PopupMenu(itemView.getContext(), btnMenu);
//                menu.inflate(R.menu.menu_user_drug_item);
//                menu.setOnMenuItemClickListener(item -> {
//                    if (item.getItemId() == R.id.action_edit) {
//                        listener.onEditClick(drug);
//                        return true;
//                    } else if (item.getItemId() == R.id.action_delete) {
//                        listener.onDeleteClick(drug);
//                        return true;
//                    }
//                    return false;
//                });
            //    menu.show();
            //  });
        }

        private String formatDate(Date date) {
            if (date == null) return "-";
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }
}
