package lk.tharaka.grocerylk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout; // Add this import

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.tharaka.grocerylk.adapters.MyOrderAdapter;
import lk.tharaka.grocerylk.models.OrderModel;

public class MyOrdersFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth auth;
    RecyclerView recyclerView;
    MyOrderAdapter orderAdapter;
    List<OrderModel> orderModelList;
    ProgressBar progressBar;
    LinearLayout emptyLayout; // Changed to LinearLayout for easier toggling

    public MyOrdersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_oders, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressBar = root.findViewById(R.id.orders_progressbar);
        recyclerView = root.findViewById(R.id.orders_rec);
        emptyLayout = root.findViewById(R.id.empty_orders_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        orderModelList = new ArrayList<>();
        orderAdapter = new MyOrderAdapter(getContext(), orderModelList);
        recyclerView.setAdapter(orderAdapter);

        loadOrderHistory();

        return root;
    }

    private void loadOrderHistory() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("CurrentUser")
                .document(auth.getCurrentUser().getUid())
                .collection("MyOrder")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        orderModelList.clear(); // Clear list to avoid duplicates on reload
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            OrderModel model = doc.toObject(OrderModel.class);
                            orderModelList.add(model);
                        }

                        // Toggle Visibility based on data
                        if (orderModelList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                            orderAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
