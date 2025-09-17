package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;

import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutItemViewHolder> {

    private List<String> itemNames;
    private List<Integer> quantities;
    private List<Double> prices;

    public CheckoutItemAdapter(List<String> itemNames, List<Integer> quantities, List<Double> prices) {
        this.itemNames = itemNames;
        this.quantities = quantities;
        this.prices = prices;
    }

    @NonNull
    @Override
    public CheckoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.checkout_item, parent, false);
        return new CheckoutItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutItemViewHolder holder, int position) {
        String name = itemNames.get(position);
        int quantity = quantities.get(position);
        double price = prices.get(position);

        holder.itemName.setText(String.format("%s x%d", name, quantity));
        holder.itemPrice.setText(String.format("LKR %d", (int) price));
    }

    @Override
    public int getItemCount() {
        return itemNames.size();
    }

    static class CheckoutItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice;

        public CheckoutItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.txtnameandquentity);
            itemPrice = itemView.findViewById(R.id.txtprice);
        }
    }
}