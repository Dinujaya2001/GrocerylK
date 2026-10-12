package lk.tharaka.grocerylk.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class OrderModel implements Serializable {
    private String orderId;
    private List<MyCartModel> orderItems; // List of all items in this specific order
    private double totalOrderAmount;
    private String orderStatus;
    private String paymentStatus;
    private Timestamp timestamp;

    public OrderModel() {}

    // Getters and Setters
    public List<MyCartModel> getOrderItems() { return orderItems; }
    public void setOrderItems(List<MyCartModel> orderItems) { this.orderItems = orderItems; }

    public double getTotalOrderAmount() { return totalOrderAmount; }
    public void setTotalOrderAmount(double totalOrderAmount) { this.totalOrderAmount = totalOrderAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
