package lk.tharaka.grocerylk;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.Serializable;
import java.util.List;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import lk.tharaka.grocerylk.models.MyCartModel;

public class PaymentActivity extends AppCompatActivity {

    TextView subTotal, discount, shipping, total;
    Button placeOrderBtn;
    Toolbar toolbar;

    private double finalTotalAmount = 0.0;
    private double deliveryCharge = 0.0; // Now dynamic
    private List<MyCartModel> cartList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        toolbar = findViewById(R.id.payment_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Checkout");
        }

        subTotal = findViewById(R.id.sub_total);
        discount = findViewById(R.id.textView17);
        shipping = findViewById(R.id.textView18);
        total = findViewById(R.id.total_amt);
        placeOrderBtn = findViewById(R.id.pay_btn);

        // RECEIVE DATA: amount from cart AND dynamic shipping from LocationFragment
        double itemAmount = getIntent().getDoubleExtra("amount", 0.0);
        deliveryCharge = getIntent().getDoubleExtra("shippingCost", 0.0);
        cartList = (List<MyCartModel>) getIntent().getSerializableExtra("item");

        // BILLING LOGIC
        finalTotalAmount = itemAmount + deliveryCharge;

        // UPDATE UI
        subTotal.setText(String.format("$%.2f", itemAmount));
        shipping.setText(String.format("$%.2f", deliveryCharge));
        total.setText(String.format("$%.2f", finalTotalAmount));
        discount.setText("$0.00");

        placeOrderBtn.setOnClickListener(v -> {
            if (cartList != null && !cartList.isEmpty()) {
                initiatePayment();
            } else {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiatePayment() {
        InitRequest req = new InitRequest();

        // --- PAYHERE CONFIGURATION ---
        req.setSandBox(true);
        req.setMerchantId("1226933");
        req.setMerchantSecret("MTYzMTEyNDIxNDE4NTkwNTMzNTIyODQyNTE0ODc1ODY5MDEwMTQ0");
        req.setCurrency("USD");

        // 1. THE CRITICAL CALCULATION: Subtotal + Shipping
        // finalTotalAmount should already be (amount + deliveryCharge) from onCreate
        req.setAmount(finalTotalAmount);

        // 2. ORDER IDENTIFICATION
        req.setOrderId("GL-" + System.currentTimeMillis());

        // 3. DESCRIPTION (This shows on the customer's receipt)
        double itemSubtotal = finalTotalAmount - deliveryCharge;
        String desc = String.format("Items: $%.2f + Shipping: $%.2f = Total: $%.2f",
                itemSubtotal, deliveryCharge, finalTotalAmount);
        req.setItemsDescription(desc);

        // --- CUSTOMER DETAILS ---
        req.getCustomer().setFirstName("GroceryLk");
        req.getCustomer().setLastName("Customer");
        req.getCustomer().setEmail("user@example.com");
        req.getCustomer().setPhone("+94770000000");

        // SENSE: If you passed an address string from Path A, use it here
        String deliveryAddress = getIntent().getStringExtra("address");
        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            deliveryAddress = "Pinned Map Location";
        }
        req.getCustomer().getAddress().setAddress(deliveryAddress);
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // --- LAUNCH PAYHERE ---
        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        paymentLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> paymentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent nextIntent = new Intent(this, PlaceOrderActivity.class);
                nextIntent.putExtra("item", (Serializable) cartList);
                nextIntent.putExtra("finalTotal", finalTotalAmount);
                nextIntent.putExtra("shippingCost", deliveryCharge);

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) result.getData().getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                    if (response != null && response.isSuccess()) {
                        nextIntent.putExtra("paymentStatus", "Paid");
                    } else {
                        nextIntent.putExtra("paymentStatus", "Failed");
                    }
                } else {
                    nextIntent.putExtra("paymentStatus", "Canceled");
                }
                startActivity(nextIntent);
                finish();
            });
}