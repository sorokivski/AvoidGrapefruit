package com.example.avoidgrapefruit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;
import com.example.avoidgrapefruit.products.DrugEntity;
import com.example.avoidgrapefruit.products.ProductDetailActivity;
import com.example.avoidgrapefruit.products.ProductEntity;

import java.util.List;

public class HomeSearchAdapter extends RecyclerView.Adapter<HomeSearchAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DisplayableItem item);
    }

    private List<DisplayableItem> items;
    private OnItemClickListener listener;
    private  final Context context;
    private String currentQuery = "";

    public HomeSearchAdapter(Context context, List<DisplayableItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayableItem item = items.get(position);
        String name = item.getName();
        holder.nameText.setText(name);
        holder.categoryText.setText(item.getCategory());

        String tags = "None";
        if (item instanceof ProductEntity) {
            List<String> tagList = ((ProductEntity) item).getTags();
            if (tagList != null && !tagList.isEmpty()) {
                tags = String.join(", ", tagList);
            }
        } else if (item instanceof DrugEntity) {
            List<String> tagList = ((DrugEntity) item).getTags();
            if (tagList != null && !tagList.isEmpty()) {
                tags = String.join(", ", tagList);
            }
        }
        holder.tagsText.setText(tags);

        // ===== Highlight search query =====
        if (!currentQuery.isEmpty()) {
            int startIndex = name.toLowerCase().indexOf(currentQuery);
            if (startIndex >= 0) {
                int endIndex = startIndex + currentQuery.length();
                Spannable spannable = new SpannableString(name);
                spannable.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#FF5722")),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                holder.nameText.setText(spannable);
            } else {
                holder.nameText.setText(name);
            }
        } else {
            holder.nameText.setText(name);
        }

        // 👉 Only delegate click handling to the listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("item", item); // make sure item implements Serializable
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateList(List<DisplayableItem> newItems, String query) {
        this.items = newItems;
        this.currentQuery = query.toLowerCase();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, categoryText, tagsText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.productName);
            categoryText = itemView.findViewById(R.id.productCategory);
            tagsText = itemView.findViewById(R.id.productTags);
        }
    }
}
