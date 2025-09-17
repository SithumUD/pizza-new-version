package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.CustomizationOption;

import java.util.ArrayList;
import java.util.List;

public class CustomizationAdapter extends RecyclerView.Adapter<CustomizationAdapter.ViewHolder> {

    private List<CustomizationOption> options;
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged();
    }

    public CustomizationAdapter(List<CustomizationOption> options, OnSelectionChangeListener listener) {
        this.options = options != null ? options : new ArrayList<>();
        this.listener = listener;
    }

    public void updateOptions(List<CustomizationOption> newOptions) {
        this.options = newOptions != null ? newOptions : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<CustomizationOption> getSelectedOptions() {
        List<CustomizationOption> selected = new ArrayList<>();
        for (CustomizationOption option : options) {
            if (option.isSelected()) {
                selected.add(option);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.customization_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CustomizationOption option = options.get(position);
        holder.bind(option);
    }

    @Override
    public int getItemCount() {
        return options.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbOption;
        private TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbOption = itemView.findViewById(R.id.cboption);
            tvPrice = itemView.findViewById(R.id.tvprice);
        }

        public void bind(CustomizationOption option) {
            cbOption.setText(option.getName());
            tvPrice.setText(String.format("LKR %.0f", option.getPrice()));
            cbOption.setChecked(option.isSelected());

            cbOption.setOnCheckedChangeListener((buttonView, isChecked) -> {
                option.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged();
                }
            });
        }
    }
}
