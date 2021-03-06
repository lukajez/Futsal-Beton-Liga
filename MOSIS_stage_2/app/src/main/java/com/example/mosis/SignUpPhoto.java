package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpPhoto extends AppCompatActivity {

    String email, userID, username, team;
    private static final int TAKE_IMAGE_CODE = 10001;
    private static final int CAMERA_REQUEST_CODE = 1;
    private StorageReference mStorageRef;
    Button btnUpload, btnBackTeam, btnUploadPhoto;
    Uri pickedImageUri;
    FirebaseAuth firebaseAuth;
    CircleImageView profileImageView;
    TextView loginL, registerL;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference docRef;
    private UserLocation userLocation;
    UserModel meModel;
    FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_photo);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }

        //region Handler za PutExtra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                email = null;
                userID = null;
                username = null;
                team = null;
            } else {
                email = extras.getString("Email");
                userID = extras.getString("UserID");
                username = extras.getString("Username");
                team = extras.getString("Team");
            }
        } else {
            email = (String) savedInstanceState.getSerializable("Email");
            userID = (String) savedInstanceState.getSerializable("UserID");
            username = (String) savedInstanceState.getSerializable("Username");
            team = (String) savedInstanceState.getSerializable("Team");
        }

        //provera za Email da li ga prenosi
        Log.d("TAG", "Persons EMAIL is: " + email);
        Log.d("TAG", "Persons USERID is: " + userID);
        Log.d("TAG", "Persons USERNAME is: " + username);
        //endregion

        mStorageRef = FirebaseStorage.getInstance().getReference("profileImages");
        btnUpload = (Button) findViewById(R.id.btnUploadPhoto);
        firebaseAuth = FirebaseAuth.getInstance();
        profileImageView = (CircleImageView) findViewById(R.id.profileImageView);

        btnBackTeam = (Button) findViewById(R.id.btnBackTeam);
        btnUploadPhoto = (Button) findViewById(R.id.btnUploadPhoto);

        docRef = fStore.document("users/" + firebaseAuth.getCurrentUser().getUid());

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference documentReference = fStore.collection("users").document(userID);
                ArrayList<String> friends = new ArrayList<>();
                ArrayList<MatchModel> matches = new ArrayList<>();
                ArrayList<MatchModel> subscriptions = new ArrayList<>();

                if(pickedImageUri != null) {
                    meModel = new UserModel(userID, email, username, 0, team, friends, matches, subscriptions, pickedImageUri.toString());
                }
                else {
                    meModel = new UserModel(userID, email, username, 0, team, friends, matches, subscriptions, "");
                }

                documentReference.set(meModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "onSuccess: user Profile is created for " + userID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: " + e.toString());
                    }
                });

                getUserData(meModel);
                goToActivity();
            }
        });

        setUpFont();
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

    protected void setUpFont(){

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        btnUpload = (Button) findViewById(R.id.btnUploadPhoto);
        btnUpload.setTypeface(typeface);
        btnBackTeam = (Button) findViewById(R.id.btnBackTeam);
        btnBackTeam.setTypeface(typeface);
        loginL = (TextView) findViewById(R.id.loginL);
        TextView txt_hint = (TextView) findViewById(R.id.txt_hint);
        registerL = (TextView) findViewById(R.id.registerL);
        loginL.setTypeface(typeface);
        registerL.setTypeface(typeface);
        txt_hint.setTypeface(typeface);
        //endregion
    }

    private void getUserData(UserModel userModel) {

        Log.d("270 TAG", "getUserData: " + userModel);

        if(userLocation == null) {

            userLocation = new UserLocation();
            userLocation.setUser(userModel);
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        Log.d("281 LAST KNOWN LOCATION", "getLastKnownLocation: called.");

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {
                    Location location = task.getResult();

                    Log.d("1463 TAG", "onComplete: location " + location);



                    if(location != null) {
                        GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        Log.d("TAG", "onComplete: latitude: " + geoPoint.getLatitude());
                        Log.d("TAG", "onComplete: longitude: " + geoPoint.getLongitude());

                        userLocation.setGeoPoint(geoPoint);
                        userLocation.setTimestamp(null);

                        Log.d("301 TAG", "onComplete: userLocation" + userLocation);
                        saveUserLocation();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });
    }

    private void saveUserLocation() {

        Log.d("314 TAG", "saveUserLocation: " + "telo funkcije " + userLocation);

        db = FirebaseFirestore.getInstance();

        if(userLocation != null){
            DocumentReference locationRef = db.collection("User Locations").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            locationRef.set(userLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TAG", "savedUserLocation: \ninsered user location into database." + "\n latitude: " + userLocation.getGeoPoint().getLatitude()+"\n longitude: " + userLocation.getGeoPoint().getLatitude());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
        }
    }

}
