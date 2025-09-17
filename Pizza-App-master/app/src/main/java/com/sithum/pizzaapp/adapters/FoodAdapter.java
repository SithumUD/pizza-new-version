package com.sithum.pizzaapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Product;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
        void onAddToCartClick(Product product);
    }

    public FoodAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_card, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvFoodName.setText(product.getName());
        holder.tvFoodPrice.setText(String.format("LKR %,.2f", product.getPrice()));
        holder.tvFoodDescription.setText(product.getDescription());
        holder.tvFoodCategory.setText(product.getCategory());

        // Load image - prioritize online image if available
        if (product.hasOnlineImage()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.pizza)
                    .error(R.drawable.pizza)
                    .into(holder.ivFoodImage);
        } else if (product.getImageResource() != 0) {
            holder.ivFoodImage.setImageResource(product.getImageResource());
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.pizza);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodImage;
        TextView tvFoodName, tvFoodPrice, tvFoodDescription, tvFoodCategory;
        Button btnAdd;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvFoodCategory = itemView.findViewById(R.id.tvFoodCategory);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}