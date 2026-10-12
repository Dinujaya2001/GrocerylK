package lk.tharaka.grocerylk;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Enable Modern Edge-to-Edge UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // 2. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // 3. Handle System Bars (Status/Navigation) Padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // --- LOGOUT LOGIC ---

    // 4. Create the Options Menu (Top Right Corner)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adding the Logout option programmatically with ID '1'
        menu.add(0, 1, 0, "Logout");
        return true;
    }

    // 5. Handle Menu Item Clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) { // 1 corresponds to the ID we set above
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 6. Perform the Logout
    private void logoutUser() {
        // Sign out from Firebase
        auth.signOut();

        // CRITICAL: Navigate to Login and clear the back-stack
        // This prevents the user from clicking 'Back' to re-enter the app
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();

        // Close HomeActivity
        finish();
    }
}