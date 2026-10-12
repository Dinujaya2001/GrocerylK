package lk.tharaka.grocerylk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.tharaka.grocerylk.models.MyCartModel;
import lk.tharaka.grocerylk.models.UserModel;

public class PlaceOrderActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_place_order);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
        }

        List<MyCartModel> list = (ArrayList<MyCartModel>) getIntent().getSerializableExtra("item");
        String paymentStatus = getIntent().getStringExtra("paymentStatus");
        double shippingCost = getIntent().getDoubleExtra("shippingCost", 0.0);  // 👈 ADD

        if (list != null && !list.isEmpty()) {
            saveOrderToFirestore(list, paymentStatus != null ? paymentStatus : "Pending", shippingCost);
        } else {
            finish();
        }
    }

    private void saveOrderToFirestore(List<MyCartModel> cartList, String pStatus, double shippingCost) {
        String userId = auth.getCurrentUser().getUid();

        database.getReference().child("Users").child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                UserModel user = task.getResult().getValue(UserModel.class);
                String userPhone = (user != null) ? user.getNumber() : null;
                processOrder(cartList, pStatus, userId, userPhone, shippingCost);  // 👈 pass
            } else {
                processOrder(cartList, pStatus, userId, null, shippingCost);  // 👈 pass
            }
        });
    }

    private void processOrder(List<MyCartModel> cartList, String pStatus, String userId, String phone, double shippingCost) {
        WriteBatch batch = firestore.batch();
        double total = 0;
        for (MyCartModel item : cartList) total += item.getTotalPrice();
        total += shippingCost;

        DocumentReference orderRef = firestore.collection("CurrentUser").document(userId)
                .collection("MyOrder").document();

        HashMap<String, Object> map = new HashMap<>();
        map.put("orderItems", cartList);
        map.put("totalOrderAmount", total);
        map.put("shippingCost", shippingCost);          // 👈 save it
        map.put("paymentStatus", pStatus);
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("orderStatus", "Paid".equals(pStatus) ? "Confirmed" : "Pending");

        batch.set(orderRef, map);

        if ("Paid".equals(pStatus)) {
            for (MyCartModel item : cartList) {
                if (item.getDocumentId() != null) {
                    batch.delete(firestore.collection("CurrentUser").document(userId).collection("AddToCart").document(item.getDocumentId()));
                }
            }
        }

        final double finalTotal = total;
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if ("Paid".equals(pStatus) && phone != null) {
                    sendSMS(cartList, finalTotal, phone);
                } else {
                    finishOrder("Order Placed!");
                }
            }
        });
    }

    private void sendSMS(List<MyCartModel> cartList, double total, String phone) {
        try {
            // Format Number for Sri Lanka (+94)
            String formattedPhone = phone.trim();
            if (formattedPhone.startsWith("0")) formattedPhone = "+94" + formattedPhone.substring(1);
            else if (!formattedPhone.startsWith("+")) formattedPhone = "+94" + formattedPhone;

            StringBuilder sb = new StringBuilder("GroceryLk Order Confirmed!\nTotal: $");
            sb.append(String.format("%.2f", total));

            // Check permission before calling hardware to prevent crash
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        ? getSystemService(SmsManager.class) : SmsManager.getDefault();
                smsManager.sendTextMessage(formattedPhone, null, sb.toString(), null, null);
                finishOrder("Order Placed & SMS Sent!");
            } else {
                finishOrder("Order Placed (SMS Permission Denied)");
            }
        } catch (Exception e) {
            finishOrder("Order Placed (SMS failed)");
        }
    }

    private void finishOrder(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}