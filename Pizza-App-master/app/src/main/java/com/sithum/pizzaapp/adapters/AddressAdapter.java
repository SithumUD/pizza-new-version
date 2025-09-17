package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private List<Address> addressList;
    private OnAddressActionListener listener;

    public interface OnAddressActionListener {
        void onEditAddress(Address address);
        void onDeleteAddress(Address address);
        void onSetAsDefault(Address address);
    }

    public AddressAdapter(List<Address> addressList, OnAddressActionListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_item, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address address = addressList.get(position);
        holder.bind(address);
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    class AddressViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvAddress, tvDefault, setAsDefaultButton;
        private ImageView btnEdit, btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvname);
            tvAddress = itemView.findViewById(R.id.tvaddress);
            tvDefault = itemView.findViewById(R.id.tvDefault);
            setAsDefaultButton = itemView.findViewById(R.id.setAsDefaultButton);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Address address) {
            tvName.setText(address.getName());
            tvAddress.setText(address.getFullAddress());

            // Show/hide default badge and set as default button
            if (address.isDefault()) {
                tvDefault.setVisibility(View.VISIBLE);
                setAsDefaultButton.setVisibility(View.GONE);
            } else {
                tvDefault.setVisibility(View.GONE);
                setAsDefaultButton.setVisibility(View.VISIBLE);
            }

            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditAddress(address);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAddress(address);
                }
            });

            setAsDefaultButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSetAsDefault(address);
                }
            });
        }
    }
}