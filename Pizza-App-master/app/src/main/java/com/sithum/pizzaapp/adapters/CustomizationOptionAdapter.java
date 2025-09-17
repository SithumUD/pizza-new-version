package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.CustomizationOption;

import java.util.List;

public class CustomizationOptionAdapter extends RecyclerView.Adapter<CustomizationOptionAdapter.OptionViewHolder> {

    private List<CustomizationOption> options;
    private OnRemoveOptionListener removeListener;

    public interface OnRemoveOptionListener {
        void onRemoveOption(CustomizationOption option);
    }

    public CustomizationOptionAdapter(List<CustomizationOption> options, OnRemoveOptionListener removeListener) {
        this.options = options;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = removeListener != null ?
                R.layout.item_customization_option :
                R.layout.item_customization_option_display;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new OptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
        CustomizationOption option = options.get(position);
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    class OptionViewHolder extends RecyclerView.ViewHolder {

        private TextView tvOptionName;
        private TextView tvOptionPrice;
        private ImageView btnRemoveOption;

        public OptionViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOptionName = itemView.findViewById(R.id.tvOptionName);
            tvOptionPrice = itemView.findViewById(R.id.tvOptionPrice);
            btnRemoveOption = itemView.findViewById(R.id.btnRemoveOption);
        }

        public void bind(CustomizationOption option) {
            tvOptionName.setText(option.getName());
            tvOptionPrice.setText("LKR " + String.format("%.0f", option.getPrice()));

            // Show/hide remove button based on whether this is in edit mode
            if (btnRemoveOption != null && removeListener != null) {
                btnRemoveOption.setVisibility(View.VISIBLE);
                btnRemoveOption.setOnClickListener(v -> {
                    if (removeListener != null) {
                        removeListener.onRemoveOption(option);
                    }
                });
            } else if (btnRemoveOption != null) {
                btnRemoveOption.setVisibility(View.GONE);
            }
        }
    }
}