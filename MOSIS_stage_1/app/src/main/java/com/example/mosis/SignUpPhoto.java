package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpPhoto extends AppCompatActivity {

    private static final int TAKE_IMAGE_CODE = 10001;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference mStorageRef;
    Button btnUpload;
    Uri pickedImageUri;
    FirebaseAuth firebaseAuth;
    CircleImageView profileImageView;

    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_photo);

        mStorageRef = FirebaseStorage.getInstance().getReference("profileImages");
        btnUpload = (Button) findViewById(R.id.btnUploadPhoto);
        firebaseAuth = FirebaseAuth.getInstance();
        profileImageView = (CircleImageView) findViewById(R.id.profileImageView);

        docRef = fStore.document("users/" + firebaseAuth.getCurrentUser().getUid());

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String userID =  firebaseAuth.getCurrentUser().getUid();

//                DocumentReference documentReference = fStore.collection("users").document(userID);
//                Map<String, Object> user = new HashMap<>();
//                user.put("profile_image_uri", pickedImageUri);
//
//                Log.d("TAG", "onSuccess PICKED DOWNLOAD URI IS: " + pickedImageUri);
//
//                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("TAG", "onSuccess: user Profile is created for " + userID);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("TAG", "onFailure: " + e.toString());
//                    }
//                });

                goToActivity();

            }
        });
    }

    private void goToActivity() {

        Intent intent = new Intent( SignUpPhoto.this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void handleImageClick(View view) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == TAKE_IMAGE_CODE) {
            switch (resultCode){
                case RESULT_OK:
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    handleUpload(bitmap);
            }
        }
    }

    private void handleUpload(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        getDownloadUrl(reference);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "onFailure: " + e.getCause());
            }
        });
    }

    private void getDownloadUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("TAG", "onSuccess DOWNLOAD URL IS: " + uri);
                        setUserProfileUrl(uri);
                    }
                });
    }

    private void setUserProfileUrl(Uri uri) {

        pickedImageUri = uri;

        Glide.with(this).load(uri).into(profileImageView);

//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//
//        Log.d("TAG TAG TAG TAG TAG", "Give me that user: " + user.getEmail());
//
//        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
//                .setPhotoUri(uri)
//                .build();
//
//        user.updateProfile(request)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(SignUpPhoto.this, "Success", Toast.LENGTH_SHORT).show();
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(SignUpPhoto.this, "Failed", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

}
