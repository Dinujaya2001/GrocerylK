package lk.tharaka.grocerylk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lk.tharaka.grocerylk.adapters.AddressAdapter;
import lk.tharaka.grocerylk.models.AddressModel;
import lk.tharaka.grocerylk.models.MyCartModel;
import lk.tharaka.grocerylk.models.NavCategoryModel;
import lk.tharaka.grocerylk.models.PopularModel;
import lk.tharaka.grocerylk.models.ViewAllModel;

public class AddressActivity extends AppCompatActivity implements AddressAdapter.SelectedAddress {

    Button addAddress;
    RecyclerView recyclerView;
    private List<AddressModel> addressModelList;
    private AddressAdapter addressAdapter;

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    Button payment_btn;
    String mAddress = "";

    // Sensing Data
    Object obj = null;
    List<MyCartModel> cartList = null;
    double fixedShippingCharge = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address);

        // 1. Get Data and Sensed Shipping from Intent
        obj = getIntent().getSerializableExtra("item");
        cartList = (List<MyCartModel>) getIntent().getSerializableExtra("item");

        // SENSE: Retrieve the $5.00 fixed charge sent from MyCartsFragment
        fixedShippingCharge = getIntent().getDoubleExtra("shippingCost", 5.0);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        addAddress = findViewById(R.id.add_address_btn);
        recyclerView = findViewById(R.id.address_recycler);
        payment_btn = findViewById(R.id.payment_btn);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        addressModelList = new ArrayList<>();
        addressAdapter = new AddressAdapter(getApplicationContext(), addressModelList, this);
        recyclerView.setAdapter(addressAdapter);

        // Fetch Addresses
        firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("Address").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                AddressModel addressModel = documentSnapshot.toObject(AddressModel.class);
                                addressModelList.add(addressModel);
                                addressAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });

        payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double amount = 0.0;

                // Calculate Subtotal (Items only)
                if (cartList != null && !cartList.isEmpty()) {
                    for (MyCartModel model : cartList) {
                        amount += model.getTotalPrice();
                    }
                } else if (obj instanceof ViewAllModel) {
                    amount = ((ViewAllModel) obj).getPrice();
                }

                if (mAddress.isEmpty()) {
                    Toast.makeText(AddressActivity.this, "Please select an address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Move to Payment Activity with BOTH amounts
                Intent intent = new Intent(AddressActivity.this, PaymentActivity.class);

                // Pack the suitcase
                intent.putExtra("amount", amount);              // Subtotal
                intent.putExtra("shippingCost", fixedShippingCharge); // Sensed $5.00
                intent.putExtra("address", mAddress);
                intent.putExtra("item", (Serializable) cartList);

                startActivity(intent);
            }
        });

        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddressActivity.this, AddAddressActivity.class));
            }
        });
    }

    @Override
    public void setAddress(String address) {
        mAddress = address;
    }
}