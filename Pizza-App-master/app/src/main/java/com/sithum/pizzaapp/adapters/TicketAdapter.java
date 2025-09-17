package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.SupportTicket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<SupportTicket> tickets;
    private OnTicketClickListener onTicketClickListener;

    public interface OnTicketClickListener {
        void onTicketClick(SupportTicket ticket);
    }

    public TicketAdapter(List<SupportTicket> tickets, OnTicketClickListener listener) {
        this.tickets = tickets;
        this.onTicketClickListener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        SupportTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public void updateTickets(List<SupportTicket> newTickets) {
        this.tickets = newTickets;
        notifyDataSetChanged();
    }

    public void removeTicket(int position) {
        tickets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tickets.size());
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSubject, tvStatus, tvOrderNumber, tvMessage, tvTimestamp, tvDevice;
        private View statusIndicator;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDevice = itemView.findViewById(R.id.tvDevice);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(v -> {
                if (onTicketClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onTicketClickListener.onTicketClick(tickets.get(position));
                    }
                }
            });
        }

        public void bind(SupportTicket ticket) {
            tvSubject.setText(ticket.getSubject());
            tvMessage.setText(ticket.getMessage());
            tvTimestamp.setText(ticket.getFormattedTimestamp());
            tvDevice.setText(ticket.getDevice() != null ? ticket.getDevice() : "Unknown");

            // Handle order number
            if (ticket.hasOrderNumber()) {
                tvOrderNumber.setVisibility(View.VISIBLE);
                tvOrderNumber.setText("Order #" + ticket.getOrderNumber());
            } else {
                tvOrderNumber.setVisibility(View.GONE);
            }

            // Set status and colors
            String status = ticket.getStatus() != null ? ticket.getStatus().toUpperCase() : "OPEN";
            tvStatus.setText(status);

            int statusColor;
            int statusBgRes;

            switch (status) {
                case "CLOSED":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_closed);
                    statusBgRes = R.drawable.status_closed_background;
                    break;
                case "IN_PROGRESS":
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_in_progress);
                    statusBgRes = R.drawable.status_in_progress_background;
                    break;
                case "OPEN":
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.status_open);
                    statusBgRes = R.drawable.status_open_background;
                    break;
            }

            statusIndicator.setBackgroundColor(statusColor);
            tvStatus.setBackgroundResource(statusBgRes);
        }
    }
}