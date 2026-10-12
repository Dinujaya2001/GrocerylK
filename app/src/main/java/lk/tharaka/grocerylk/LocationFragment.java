package lk.tharaka.grocerylk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.List;

import lk.tharaka.grocerylk.models.MyCartModel;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Marker markerPin;
    private Polyline directionLine;

    // GroceryLk Shop Base (Colombo)
    private final LatLng shopBase = new LatLng(6.9271, 79.8612);
    private double currentShippingCost = 0.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Ensure your XML uses a RelativeLayout or FrameLayout so the button is visible
        View view = inflater.inflate(R.layout.activity_location_fragment, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // SENSE: Only move camera automatically if the user hasn't set a pin yet
                    if (location != null && mMap != null && markerPin == null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 16f));
                    }
                }
            }
        };

        // Confirm Button Logic
        Button btnConfirm = view.findViewById(R.id.btn_confirm_location);
        btnConfirm.setOnClickListener(v -> {
            if (markerPin != null) {
                // Collect cart data from activity intent
                double subtotal = getActivity().getIntent().getDoubleExtra("amount", 0.0);
                List<MyCartModel> cartList = (List<MyCartModel>) getActivity().getIntent().getSerializableExtra("item");

                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                intent.putExtra("userLat", markerPin.getPosition().latitude);
                intent.putExtra("userLon", markerPin.getPosition().longitude);
                intent.putExtra("shippingCost", currentShippingCost);
                intent.putExtra("amount", subtotal);
                intent.putExtra("item", (Serializable) cartList);

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Please long-click map to set your house pin", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Add Shop Marker
        mMap.addMarker(new MarkerOptions().position(shopBase).title("GroceryLk Base"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopBase, 15f));

        enableMyLocation();

        mMap.setOnMapLongClickListener(latLng -> {
            if (markerPin == null) {
                markerPin = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Delivery Spot")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            } else {
                markerPin.setPosition(latLng);
            }
            // SENSE: Update directions and calculate shipping cost
            drawDirections(latLng);
        });
    }

    private void drawDirections(LatLng userLatLng) {
        if (directionLine != null) directionLine.remove();

        // 1. Calculate Distance
        float[] results = new float[1];
        Location.distanceBetween(shopBase.latitude, shopBase.longitude,
                userLatLng.latitude, userLatLng.longitude, results);
        float distanceKm = results[0] / 1000;

        // 2. Shipping Logic (2km Free, else $5)
        int lineColor;
        String feeMsg;
        if (distanceKm <= 2.0) {
            currentShippingCost = 0.0;
            lineColor = Color.BLUE;
            feeMsg = "Free Delivery! (" + String.format("%.2f", distanceKm) + " km)";
        } else {
            currentShippingCost = 5.0;
            lineColor = Color.RED;
            feeMsg = "Shipping Fee: $5.00 (" + String.format("%.2f", distanceKm) + " km)";
        }

        // 3. Draw Path
        directionLine = mMap.addPolyline(new PolylineOptions()
                .add(shopBase, userLatLng)
                .width(10f)
                .color(lineColor)
                .geodesic(true));

        Toast.makeText(getContext(), feeMsg, Toast.LENGTH_SHORT).show();
    }

    private void enableMyLocation() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            startLocationUpdates();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationClient != null) fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}