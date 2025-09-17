package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onItemRemoved(CartItem cartItem, int position);
        void onQuantityIncreased(CartItem cartItem, int position);
        void onQuantityDecreased(CartItem cartItem, int position);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemName, itemOptions, itemPrice, itemQuantity;
        private ImageButton btnDecrease, btnIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemOptions = itemView.findViewById(R.id.itemOptions);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemQuantity = itemView.findViewById(R.id.itemQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);

            btnDecrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onQuantityDecreased(cartItems.get(position), position);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onQuantityIncreased(cartItems.get(position), position);
                }
            });
        }

        public void bind(CartItem item) {
            itemName.setText(item.getProductName());
            itemOptions.setText(item.getOptionsString());
            itemPrice.setText(String.format("LKR %d", (int)(item.getTotalPrice() / item.getQuantity())));
            itemQuantity.setText(String.valueOf(item.getQuantity()));

            // Set image (you can use Glide/Picasso for actual images)
            itemImage.setImageResource(R.drawable.pizza);
        }
    }
}