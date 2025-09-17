package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Branch;

import java.util.List;

public class BranchLocationAdapter extends RecyclerView.Adapter<BranchLocationAdapter.BranchViewHolder> {

    private List<Branch> branchList;
    private OnBranchClickListener listener;

    public interface OnBranchClickListener {
        void onBranchClick(Branch branch);
    }

    public BranchLocationAdapter(List<Branch> branchList, OnBranchClickListener listener) {
        this.branchList = branchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_branch, parent, false);
        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
        Branch branch = branchList.get(position);
        holder.bind(branch, listener);
    }

    @Override
    public int getItemCount() {
        return branchList.size();
    }

    public static class BranchViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBranchName, tvBranchAddress, tvBranchManager, tvBranchStatus;

        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvBranchAddress = itemView.findViewById(R.id.tvBranchAddress);
            tvBranchManager = itemView.findViewById(R.id.tvBranchManager);
            tvBranchStatus = itemView.findViewById(R.id.tvBranchStatus);
        }

        public void bind(Branch branch, OnBranchClickListener listener) {
            tvBranchName.setText(branch.getName());
            tvBranchAddress.setText(branch.getAddress());

            // Set manager info
            if (branch.getManager() != null && !branch.getManager().isEmpty()) {
                tvBranchManager.setText("Manager: " + branch.getManager() + " • 5.2 km away");
            } else {
                tvBranchManager.setText("5.2 km away"); // Default distance
            }

            // Set status
            if ("active".equals(branch.getStatus())) {
                tvBranchStatus.setText("Active");
                tvBranchStatus.setBackgroundResource(R.drawable.status_active_bg);
            } else {
                tvBranchStatus.setText("Inactive");
                tvBranchStatus.setBackgroundResource(R.drawable.status_inactive_bg);
            }

            itemView.setOnClickListener(v -> listener.onBranchClick(branch));
        }
    }
}