package lk.tharaka.grocerylk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddAddressActivity extends AppCompatActivity {

    EditText name, address, city, code, phone;
    Button addAddressBtn, btnGetLocation; // Added location button

    FirebaseFirestore firestore;
    FirebaseAuth auth;

    // Location Sensor Hardware API
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_address);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Views
        name = findViewById(R.id.ad_name);
        address = findViewById(R.id.ad_address);
        city = findViewById(R.id.ad_city);
        code = findViewById(R.id.ad_code);
        phone = findViewById(R.id.ad_phone);
        addAddressBtn = findViewById(R.id.ad_add_address);

        // Ensure you have a button in your XML with this ID: get_location_btn
        btnGetLocation = findViewById(R.id.get_location_btn);

        // Click to get current GPS location
        btnGetLocation.setOnClickListener(v -> checkLocationPermission());

        // Click to upload to Firebase
        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAddress();
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Requesting runtime permission for the sensor
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();
            if (location != null) {
                // Sense the coordinates
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Convert sensor numbers to human-readable address
                getAddressFromCoords(latitude, longitude);
            } else {
                Toast.makeText(this, "Please enable GPS in settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAddressFromCoords(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address currentAddr = addresses.get(0);

                // Automatically fill the fields for the user
                address.setText(currentAddr.getAddressLine(0));
                city.setText(currentAddr.getLocality());
                code.setText(currentAddr.getPostalCode());

                Toast.makeText(this, "Location detected!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Service unavailable. Try typing manually.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadAddress() {
        String userName = name.getText().toString().trim();
        String userCity = city.getText().toString().trim();
        String userAddress = address.getText().toString().trim();
        String userCode = code.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();

        if (userName.isEmpty() || userCity.isEmpty() || userAddress.isEmpty() ||
                userCode.isEmpty() || userPhone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("userName", userName);
        map.put("userCity", userCity);
        map.put("userAddress", userAddress);
        map.put("userPostalCode", userCode);
        map.put("userPhone", userPhone);

        firestore.collection("CurrentUser")
                .document(auth.getCurrentUser().getUid())
                .collection("Address")
                .add(map)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddAddressActivity.this, "Address Saved", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(AddAddressActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permission denied to use GPS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}