package com.example.avoidgrapefruit.products;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<ProductEntity> productList;

    public ProductAdapter(Context context, List<ProductEntity> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void updateList(List<ProductEntity> newList) {
        this.productList = newList;
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
        ProductEntity product = productList.get(position);
        holder.nameText.setText(product.getName());
        holder.categoryText.setText(product.getCategory());
        holder.tagsText.setText(product.getTags() != null ? product.getTags().toString() : "No tags");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
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
