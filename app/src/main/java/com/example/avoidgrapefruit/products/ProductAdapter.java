package com.example.avoidgrapefruit.products;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final Context context;
    private List<DisplayableItem> itemList;

    public ProductAdapter(Context context, List<DisplayableItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void updateList(List<DisplayableItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        DisplayableItem item = itemList.get(position);

        holder.nameText.setText(item.getName());
        holder.categoryText.setText(item.getCategory());

        // ===== Set tags safely =====
        String tags = "None";
        if (item instanceof ProductEntity) {
            List<String> tagList = ((ProductEntity) item).getTags();
            if (tagList != null && !tagList.isEmpty()) {
                tags = String.join(", ", tagList);
            }

            // Log important fields to check Firestore mapping
            Log.d("ProductAdapter", "Product: " + ((ProductEntity) item).getName()
                    + ", imageUrl: " + ((ProductEntity) item).getImageUrl()
                    + ", nutritionalComponents: " + ((ProductEntity) item).getNutritionalComponents()
                    + ", regionalDishes: " + ((ProductEntity) item).getRegionalDishes());
        } else if (item instanceof DrugEntity) {
            List<String> tagList = ((DrugEntity) item).getTags();
            if (tagList != null && !tagList.isEmpty()) {
                tags = String.join(", ", tagList);
            }
        }
        holder.tagsText.setText(tags);

        // ===== Click to open ProductDetailActivity =====
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("item", item); // make sure item implements Serializable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, categoryText, tagsText;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.productName);
            categoryText = itemView.findViewById(R.id.productCategory);
            tagsText = itemView.findViewById(R.id.productTags);
        }
    }
}
