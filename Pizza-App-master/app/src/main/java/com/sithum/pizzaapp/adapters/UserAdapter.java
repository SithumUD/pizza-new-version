package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
        void onUserLongClick(User user);
    }

    public UserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUserName, tvUserEmail, tvUserMeta, tvUserType;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserMeta = itemView.findViewById(R.id.tvUserMeta);
            tvUserType = itemView.findViewById(R.id.tvUserType);
        }

        public void bind(User user, OnUserClickListener listener) {
            tvUserName.setText(user.getFullName());
            tvUserEmail.setText(user.getEmail());
            tvUserType.setText(user.getRole());

            // Set meta information based on user type
            if ("customer".equalsIgnoreCase(user.getRole())) {
                tvUserMeta.setText("12 orders"); // You would fetch this from Firestore
            } else {
                tvUserMeta.setText("Staff member");
            }

            itemView.setOnClickListener(v -> listener.onUserClick(user));
            itemView.setOnLongClickListener(v -> {
                listener.onUserLongClick(user);
                return true;
            });
        }
    }
}