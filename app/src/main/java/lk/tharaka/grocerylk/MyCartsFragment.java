package lk.tharaka.grocerylk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import lk.tharaka.grocerylk.adapters.MyCartAdapter;
import lk.tharaka.grocerylk.models.MyCartModel;

public class MyCartsFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    TextView overTotalAmount;
    RecyclerView recyclerView;
    MyCartAdapter cartAdapter;
    List<MyCartModel> cartModelList;

    ProgressBar progressBar;
    Button buyNow;

    public MyCartsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_carts, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressBar = root.findViewById(R.id.progressbar);
        overTotalAmount = root.findViewById(R.id.textView6);
        buyNow = root.findViewById(R.id.buy_now);
        recyclerView = root.findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        cartModelList = new ArrayList<>();
        cartAdapter = new MyCartAdapter(getContext(), cartModelList);
        recyclerView.setAdapter(cartAdapter);

        // Show progress bar while loading
        progressBar.setVisibility(View.VISIBLE);

        db.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                .collection("AddToCart")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                                String documentId = documentSnapshot.getId();
                                MyCartModel myCartModel = documentSnapshot.toObject(MyCartModel.class);
                                if (myCartModel != null) {
                                    myCartModel.setDocumentId(documentId);
                                    cartModelList.add(myCartModel);
                                }
                            }
                            cartAdapter.notifyDataSetChanged();
                            calculateTotalAmount(cartModelList);
                        }
                    }
                });

        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartModelList != null && !cartModelList.isEmpty()) {

                    // 1. Create a wrapper with your custom style
                    Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupMenuStyle);
                    androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(wrapper, buyNow);

                    // 2. Add items with Icons
                    popup.getMenu().add(0, 1, 0, "Normal Checkout (Saved Address)")
                            .setIcon(R.drawable.ic_home); // Ensure this exists in res/drawable

                    popup.getMenu().add(0, 2, 1, "Fast Checkout (Select on Map)")
                            .setIcon(R.drawable.ic_location); // Ensure this exists in res/drawable

                    // 3. FORCE ICONS TO SHOW (The Sensing Trick using Reflection)
                    try {
                        java.lang.reflect.Field field = popup.getClass().getDeclaredField("mPopup");
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 4. Handle Clicks
                    popup.setOnMenuItemClickListener(item -> {
                        double subtotal = 0.0;
                        for (MyCartModel m : cartModelList) subtotal += m.getTotalPrice();

                        switch (item.getItemId()) {
                            case 1: // Normal Path
                                Intent normalIntent = new Intent(getActivity(), AddressActivity.class);
                                normalIntent.putExtra("item", (Serializable) cartModelList);
                                normalIntent.putExtra("amount", subtotal);
                                normalIntent.putExtra("shippingCost", 5.0); // Path 1: Fixed $5
                                startActivity(normalIntent);
                                return true;

                            case 2: // Fast Path
                                Intent fastIntent = new Intent(getActivity(), MainActivity.class);
                                fastIntent.putExtra("item", (Serializable) cartModelList);
                                fastIntent.putExtra("amount", subtotal);
                                fastIntent.putExtra("target", "location_map"); // Sensing flag
                                startActivity(fastIntent);
                                return true;

                            default:
                                return false;
                        }
                    });

                    popup.show();

                } else {
                    Toast.makeText(getActivity(), "Cart is empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    private void calculateTotalAmount(List<MyCartModel> cartModelList) {
        double totalAmount = 0.0;
        for (MyCartModel myCartModel : cartModelList) {
            totalAmount += myCartModel.getTotalPrice();
        }
        overTotalAmount.setText(String.format("Total Amount: $%.2f", totalAmount));
    }
}