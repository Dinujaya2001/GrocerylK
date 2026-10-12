package lk.tharaka.grocerylk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import lk.tharaka.grocerylk.models.UserModel;

public class ProfileFragment extends Fragment {

    ImageView imageView;
    EditText name, email, number, address;
    Button update;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        imageView = root.findViewById(R.id.Profile_img);
        name = root.findViewById(R.id.profile_name);
        email = root.findViewById(R.id.profile_email);
        number = root.findViewById(R.id.profile_number);
        address = root.findViewById(R.id.profile_address);
        update = root.findViewById(R.id.update);

        // 1. Load Current User Data from Realtime Database
        database.getReference().child("Users").child(auth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null) {
                            name.setText(userModel.getName());
                            email.setText(userModel.getEmail());
                            number.setText(userModel.getNumber());
                            address.setText(userModel.getAddress());

                            if (getContext() != null && userModel.getProfileImg() != null) {
                                Glide.with(getContext()).load(userModel.getProfileImg()).into(imageView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                });

        // 2. Image Selection
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 33);
        });

        // 3. Update Button Click
        update.setOnClickListener(v -> updateUserProfile());

        return root;
    }

    private void updateUserProfile() {
        String uName = name.getText().toString();
        String uNumber = number.getText().toString();
        String uAddress = address.getText().toString();

        if (uName.isEmpty() || uNumber.isEmpty() || uAddress.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use HashMap to update specific fields without overwriting the whole object
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", uName);
        map.put("number", uNumber);
        map.put("address", uAddress);

        database.getReference().child("Users").child(auth.getUid())
                .updateChildren(map)
                .addOnSuccessListener(unused ->
                        Toast.makeText(getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 33 && data != null && data.getData() != null) {
            Uri profileUri = data.getData();
            imageView.setImageURI(profileUri);

            final StorageReference reference = storage.getReference().child("profile_picture")
                    .child(auth.getUid());

            reference.putFile(profileUri).addOnSuccessListener(taskSnapshot -> {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    database.getReference().child("Users").child(auth.getUid())
                            .child("profileImg").setValue(uri.toString());
                    Toast.makeText(getContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }
}