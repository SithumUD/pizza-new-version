package com.sithum.pizzaapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.PaymentMethod;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    private List<PaymentMethod> paymentMethods;
    private Context context;
    private OnPaymentMethodListener listener;

    public interface OnPaymentMethodListener {
        void onDeleteClick(PaymentMethod paymentMethod, int position);
        void onSetDefaultClick(PaymentMethod paymentMethod, int position);
    }

    public PaymentMethodAdapter(Context context, List<PaymentMethod> paymentMethods) {
        this.context = context;
        this.paymentMethods = paymentMethods;
    }

    public void setOnPaymentMethodListener(OnPaymentMethodListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.payment_method_item, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethod paymentMethod = paymentMethods.get(position);
        holder.bind(paymentMethod);
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public void updateList(List<PaymentMethod> newPaymentMethods) {
        this.paymentMethods = newPaymentMethods;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        paymentMethods.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(int position, PaymentMethod paymentMethod) {
        paymentMethods.set(position, paymentMethod);
        notifyItemChanged(position);
    }

    class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private ImageView cardIcon, deleteIcon;
        private TextView cardTitle, cardNumber, cardExpiry, setAsDefaultButton;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIcon = itemView.findViewById(R.id.cardIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            cardTitle = itemView.findViewById(R.id.cardTitle);
            cardNumber = itemView.findViewById(R.id.cardNumber);
            cardExpiry = itemView.findViewById(R.id.cardExpiry);
            setAsDefaultButton = itemView.findViewById(R.id.setAsDefaultButton);
        }

        public void bind(PaymentMethod paymentMethod) {
            // Set card type and icon
            setupCardTypeUI(paymentMethod.getCardType());

            // Set card details
            cardNumber.setText(paymentMethod.getMaskedCardNumber());
            cardExpiry.setText("Expires: " + paymentMethod.getExpiryDate());

            // Handle default status
            if (paymentMethod.isDefault()) {
                setAsDefaultButton.setText("Default");
                setAsDefaultButton.setTextColor(context.getColor(R.color.green));
                setAsDefaultButton.setEnabled(false);
            } else {
                setAsDefaultButton.setText("Set as Default");
                setAsDefaultButton.setTextColor(context.getColor(R.color.red));
                setAsDefaultButton.setEnabled(true);
            }

            // Set click listeners
            deleteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(paymentMethod, getAdapterPosition());
                }
            });

            setAsDefaultButton.setOnClickListener(v -> {
                if (listener != null && !paymentMethod.isDefault()) {
                    listener.onSetDefaultClick(paymentMethod, getAdapterPosition());
                }
            });
        }

        private void setupCardTypeUI(String cardType) {
            switch (cardType.toLowerCase()) {
                case "visa":
                    cardTitle.setText("Visa");
                    cardIcon.setBackgroundColor(context.getColor(R.color.blue));
                    cardIcon.setImageResource(R.drawable.ic_visa); // You'll need to add this
                    break;
                case "mastercard":
                    cardTitle.setText("Mastercard");
                    cardIcon.setBackgroundColor(context.getColor(R.color.red));
                    cardIcon.setImageResource(R.drawable.ic_mastercard); // You'll need to add this
                    break;
                case "amex":
                    cardTitle.setText("American Express");
                    cardIcon.setBackgroundColor(context.getColor(R.color.blue_dark));
                    cardIcon.setImageResource(R.drawable.ic_amex); // You'll need to add this
                    break;
                default:
                    cardTitle.setText("Card");
                    cardIcon.setBackgroundColor(context.getColor(R.color.grey));
                    cardIcon.setImageResource(R.drawable.ic_credit_card); // You'll need to add this
                    break;
            }
        }
    }
}