package lk.tharaka.grocerylk.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.tharaka.grocerylk.R;
import lk.tharaka.grocerylk.models.MyCartModel;
import lk.tharaka.grocerylk.models.OrderModel;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder> {

    private Context context;
    private List<OrderModel> list;

    public MyOrderAdapter(Context context, List<OrderModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.my_order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderModel model = list.get(position);

        // 1. Join item names into a single string: "Apple, Milk, Bread"
        StringBuilder itemNames = new StringBuilder();
        if (model.getOrderItems() != null) {
            for (MyCartModel item : model.getOrderItems()) {
                itemNames.append(item.getProductName()).append(", ");
            }
        }

        // Remove the trailing comma and space
        String displayNames = itemNames.toString();
        if (displayNames.length() > 2) {
            displayNames = displayNames.substring(0, displayNames.length() - 2);
        }

        // 2. Set the text views
        holder.name.setText(displayNames);
        holder.price.setText(String.format("$%.2f", model.getTotalOrderAmount()));

        // Show total item count instead of single qty
        if (model.getOrderItems() != null) {
            holder.quantity.setText(model.getOrderItems().size() + " Items");
        }

        // 3. Display and Style the Status
        holder.status.setText(model.getOrderStatus());

        if ("Confirmed".equals(model.getOrderStatus())) {
            holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.status.setBackgroundResource(R.drawable.status_bg_green);
        } else if ("Canceled".equals(model.getOrderStatus())) {
            holder.status.setTextColor(Color.parseColor("#F44336")); // Red
            holder.status.setBackgroundResource(R.drawable.status_bg_red);
        } else {
            holder.status.setTextColor(Color.parseColor("#FF9800")); // Orange
            holder.status.setBackgroundResource(R.drawable.status_bg_orange);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, quantity, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.order_product_name);
            price = itemView.findViewById(R.id.order_total_price);
            quantity = itemView.findViewById(R.id.order_qty);
            status = itemView.findViewById(R.id.order_status_txt);
        }
    }
}
