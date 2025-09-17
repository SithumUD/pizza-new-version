package com.sithum.pizzaapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.sithum.pizzaapp.R;
import com.sithum.pizzaapp.models.Product;

import java.util.List;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditProduct(Product product, int position);
        void onDeleteProduct(Product product, int position);
        void onProductClick(Product product, int position);
    }

    public ProductManageAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_manage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, position);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductDescription;
        private TextView tvProductPrice;
        private TextView tvProductCategory;
        private TextView tvProductStatus;
        private ImageView btnEdit;
        private ImageView btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProductImage = itemView.findViewById(R.id.productImage); // Was R.id.ivProductImage
            tvProductName = itemView.findViewById(R.id.productName); // Was R.id.tvProductName
            tvProductDescription = itemView.findViewById(R.id.productDescription); // Was R.id.tvProductDescription
            tvProductPrice = itemView.findViewById(R.id.productPrice); // Was R.id.tvProductPrice
            tvProductCategory = itemView.findViewById(R.id.productCategory); // Was R.id.tvProductCategory
            tvProductStatus = itemView.findViewById(R.id.productStatus); // Was R.id.tvProductStatus
            btnEdit = itemView.findViewById(R.id.editButton); // This one is correct
            btnDelete = itemView.findViewById(R.id.deleteButton); // This one is correct
        }

        public void bind(Product product, int position) {
            tvProductName.setText(product.getName());
            tvProductDescription.setText(product.getDescription());
            tvProductPrice.setText("LKR " + String.format("%.0f", product.getPrice()));
            tvProductCategory.setText(product.getCategory());
            tvProductStatus.setText(product.getStatus());

            // Set status text color
            if ("Available".equals(product.getStatus())) {
                tvProductStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.green));
            } else {
                tvProductStatus.setTextColor(itemView.getContext().getResources().getColor(R.color.red));
            }

            // Load product image with Glide
            loadProductImage(product);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product, position);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditProduct(product, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProduct(product, position);
                }
            });
        }

        private void loadProductImage(Product product) {
            try {
                if (product.hasOnlineImage()) {
                    // Load image from Cloudinary URL
                    Glide.with(itemView.getContext())
                            .load(product.getImageUrl())
                            .transform(new RoundedCorners(16))
                            .placeholder(R.drawable.pizza)
                            .error(R.drawable.pizza)
                            .into(ivProductImage);
                } else {
                    // Load local drawable resource
                    Glide.with(itemView.getContext())
                            .load(product.getImageResource())
                            .transform(new RoundedCorners(16))
                            .placeholder(R.drawable.pizza)
                            .error(R.drawable.pizza)
                            .into(ivProductImage);
                }
            } catch (Exception e) {
                // Fallback to default image
                ivProductImage.setImageResource(R.drawable.pizza);
            }
        }
    }
}