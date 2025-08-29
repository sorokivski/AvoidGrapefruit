package com.example.avoidgrapefruit.products;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interfaces.DisplayableItem;

import java.util.List;
import java.util.stream.Collectors;

public class MixedItemAdapter extends RecyclerView.Adapter<MixedItemAdapter.ViewHolder> {

    private List<DisplayableItem> itemList;

    public MixedItemAdapter(List<DisplayableItem> itemList) {
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProduct;
        TextView productName, productCategory, productTags;

        public ViewHolder(View view) {
            super(view);
            imageProduct = view.findViewById(R.id.imageProduct);
            productName = view.findViewById(R.id.productName);
            productCategory = view.findViewById(R.id.productCategory);
            productTags = view.findViewById(R.id.productTags);
        }
    }

    @Override
    public MixedItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DisplayableItem item = itemList.get(position);
        holder.productName.setText(item.getName());
        holder.productCategory.setText(item.getCategory());

        List<String> tags = null;
        if (item instanceof ProductEntity) {
            tags = ((ProductEntity) item).getTags();
        } else if (item instanceof DrugEntity) {
            tags = ((DrugEntity) item).getTags();
        }
        if (tags != null && !tags.isEmpty()) {
            holder.productTags.setText(tags.stream().collect(Collectors.joining(", ")));
        } else {
            holder.productTags.setText("No tags");
        }

        holder.imageProduct.setImageResource(R.drawable.placeholder);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("products", item); // item must be Serializable or Parcelable
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<DisplayableItem> newItems) {
        itemList = newItems;
        notifyDataSetChanged();
    }
}