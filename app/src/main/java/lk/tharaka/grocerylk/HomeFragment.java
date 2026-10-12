package lk.tharaka.grocerylk;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.tharaka.grocerylk.adapters.HomeAdapter;
import lk.tharaka.grocerylk.adapters.PopularAdapter;
import lk.tharaka.grocerylk.adapters.RecommendedAdapter;
import lk.tharaka.grocerylk.adapters.ViewAllAdapter;
import lk.tharaka.grocerylk.models.HomeCategory;
import lk.tharaka.grocerylk.models.PopularModel;
import lk.tharaka.grocerylk.models.RecommendedModel;
import lk.tharaka.grocerylk.models.ViewAllModel;

public class HomeFragment extends Fragment {

    ScrollView scrollView;
    ProgressBar progressBar;

    RecyclerView popularRec, homeRec, recommendedRec;
    FirebaseFirestore db;

    // popular Item
    List<PopularModel> PopularModelList;
    PopularAdapter popularAdapter;

    /// //Search Item
    EditText search_box;
    private List<ViewAllModel> viewAllModelList;
    private List<ViewAllModel> allProductsList;
    private RecyclerView recyclerViewSearch;
    private ViewAllAdapter viewAllAdapter;

    //Home Category
    List<HomeCategory> categoryList;
    HomeAdapter homeAdapter;

    //Recommended item
    List<RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        popularRec = root.findViewById(R.id.pop_rec);
        homeRec = root.findViewById(R.id.explore_rec);
        recommendedRec = root.findViewById(R.id.recommended_rec);
        scrollView = root.findViewById(R.id.scrollView);
        progressBar = root.findViewById(R.id.progressbar);

        progressBar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        // popular Item
        popularRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        PopularModelList = new ArrayList<>();
        popularAdapter = new PopularAdapter(getActivity(), PopularModelList);
        popularRec.setAdapter(popularAdapter);

        db.collection("PopularProducts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        PopularModel popularModel = document.toObject(PopularModel.class);
                        PopularModelList.add(popularModel);
                    }
                    popularAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Home Category Item
        homeRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        homeAdapter = new HomeAdapter(getActivity(), categoryList);
        homeRec.setAdapter(homeAdapter);

        db.collection("HomeCategory").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        HomeCategory homeCategory = document.toObject(HomeCategory.class);
                        categoryList.add(homeCategory);
                    }
                    homeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Recommended Item
        recommendedRec.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
        recommendedModelList = new ArrayList<>();
        recommendedAdapter = new RecommendedAdapter(getActivity(), recommendedModelList);
        recommendedRec.setAdapter(recommendedAdapter);

        db.collection("Recommended").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        RecommendedModel recommendedModel = document.toObject(RecommendedModel.class);
                        recommendedModelList.add(recommendedModel);
                    }
                    recommendedAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), "Error" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        /// //Search Item Setup
        recyclerViewSearch = root.findViewById(R.id.search_result_rec);
        search_box = root.findViewById(R.id.search_box);

        viewAllModelList = new ArrayList<>();
        allProductsList = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(getContext(), viewAllModelList);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearch.setAdapter(viewAllAdapter);
        recyclerViewSearch.setHasFixedSize(true);


        db.collection("AllProducts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        ViewAllModel viewAllModel = doc.toObject(ViewAllModel.class);
                        allProductsList.add(viewAllModel);
                    }
                }
            }
        });


        search_box.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    viewAllModelList.clear();
                    viewAllAdapter.notifyDataSetChanged();
                } else {
                    searchProduct(s.toString());
                }
            }
        });

        return root;
    }


    private void searchProduct(String text) {
        viewAllModelList.clear();

        if (!text.isEmpty()) {
            text = text.toLowerCase();

            for (ViewAllModel product : allProductsList) {


                boolean matchName = product.getName() != null && product.getName().toLowerCase().contains(text);
                boolean matchType = product.getType() != null && product.getType().toLowerCase().contains(text);

                if (matchName || matchType) {
                    viewAllModelList.add(product);
                }
            }
        }

        viewAllAdapter.notifyDataSetChanged();
    }
}