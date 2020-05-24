package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class AddMatchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView txtDateAddMatch;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Button btn_Confirm_AddMatch;
    private EditText etxt_Name_AddMatch;
    private ImageView match_image_AddMatch;
    private static final int TAKE_IMAGE_CODE = 10001;

    private boolean fieldName = false;
    private boolean fieldType = true;
    private boolean fieldDate = false;
    private String matchId = "";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private MatchLocation matchLocation;
    private MatchModel matchModel;
    private UserModel userModel;

    private Uri pickedImageUri;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_match);

        txtDateAddMatch = (TextView) findViewById(R.id.txt_Date_AddMatch);
        etxt_Name_AddMatch = findViewById(R.id.etxt_Name_AddMatch);
        btn_Confirm_AddMatch = findViewById(R.id.btn_Confirm_AddMatch);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getApplicationContext()));

        etxt_Name_AddMatch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().length() > 0)
                    fieldName = true;
                else
                    fieldName = false;
            }
        });

        txtDateAddMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog( AddMatchActivity.this, android.R.style.Theme_Holo_Dialog_MinWidth,
                        dateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                month += 1;
                Log.d("TAG", "onDateSet: date: " + year + "/" + month + "/" + dayOfMonth);

                String date = month + "/" + dayOfMonth + "/" + year;
                txtDateAddMatch.setText(date);
                fieldDate = true;
            }
        };

        final Spinner spinnerAddMatch = findViewById(R.id.spinnerAddMatch);

        MySpinnerAdapter adapter = new MySpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                Arrays.asList(getResources().getStringArray(R.array.spinnerValues))
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAddMatch.setAdapter(adapter);
        spinnerAddMatch.setOnItemSelectedListener(this);

        Button btn_Back_AddMatch = findViewById(R.id.btn_Back_AddMatch);
        btn_Back_AddMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent intent = new Intent(AddMatchActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                } finally {
                    finish();
                }
            }
        });

        btn_Confirm_AddMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMatchData(etxt_Name_AddMatch.getText().toString(), spinnerAddMatch.getSelectedItem().toString(), txtDateAddMatch.getText().toString());
            }
        });

        match_image_AddMatch = findViewById(R.id.match_image_AddMatch);

        setUpFont();
    }

    @Override
    protected void onResume() {

        btn_Confirm_AddMatch = findViewById(R.id.btn_Confirm_AddMatch);

        super.onResume();
        if(fieldName && fieldType && fieldDate)
            btn_Confirm_AddMatch.setVisibility(View.VISIBLE);
        else btn_Confirm_AddMatch.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT);
        fieldType = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        fieldType = false;
    }


    private void setMatchData(final String name, final String type, final String date) {

        db = FirebaseFirestore.getInstance();


        if(matchModel == null)
            matchModel = new MatchModel();

        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    userModel = task.getResult().toObject(UserModel.class);

                    ArrayList<UserModel> userModels = new ArrayList<>();
                    userModels.add(userModel);
                    matchModel.setCreator(userModel);
                    matchModel.setName(name);
                    matchModel.setType(type);
                    matchModel.setDate(date);
                    matchModel.setPartitipiants(userModels);

                    setMatchInFirestore();

                    //updateUserInFirestore();
                }
            }
        });

    }

    private void setMatchInFirestore() {

        db.collection("Matches").add(matchModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

                matchModel.setId(documentReference.getId());
                updateMatchInFirebase();
                updateUserInFirestore();

                try {
                    Intent intent = new Intent(AddMatchActivity.this, HomeActivity.class);
                    startActivity(intent);
                    //overridePendingTransition(0, 0);

                } finally {
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });
    }

    private void updateMatchInFirebase() {

        if(matchModel != null) {
            db = FirebaseFirestore.getInstance();
            db.collection("Matches").document(matchModel.getId()).update("id", matchModel.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("246: TAG:", "updateMatchInFirebase: done");
                    getMatchLocation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
        }
    }

    private void setMatchLocationInFirebase(final MatchModel matchModel) {


        //matchLocation.setMatch(matchModel);

        db = FirebaseFirestore.getInstance();

        db.collection("Match Locations").document(matchModel.getId()).set(matchModel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d("268..........TAG", "savedMatchLocation: \ninserted match location into database." + "\n latitude: " + matchLocation.getGeoPoint().getLatitude()+"\n longitude: " + matchLocation.getGeoPoint().getLatitude());
                }
            }
        });
    }

    private void getMatchLocation() {

        if(matchLocation == null)
            matchLocation = new MatchLocation();

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()) {

                    Location location = task.getResult();
                    assert location != null;
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    Log.d("299: TAG:", "onComplete: latitude: " + geoPoint.getLatitude());
                    Log.d("300: TAG:", "onComplete: longitude: " + geoPoint.getLongitude());

                    matchLocation.setGeoPoint(geoPoint);
                    matchLocation.setTimestamp(null);
                    matchLocation.setMatch(matchModel);
                    matchLocation.setLocation_name(matchModel.getName());
                    matchLocation.setMatch_type(matchModel.getType());

                    saveMatchLocation();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });
    }

    private void saveMatchLocation() {



        Log.d("321: TAG:", "onComplete: latitude: " + matchLocation + " matchModel.getId(): " + matchModel.getId());

        if(matchLocation != null){

            Log.d("325: TAG:", "onComplete: latitude: " + matchLocation + " matchModel.getId(): " + matchModel.getId());

            db = FirebaseFirestore.getInstance();

            db.collection("Match Locations").document(matchModel.getId()).set(matchLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("330 TAG", "savedUserLocation: \ninsered user location into database." + "\n latitude: " + matchLocation.getGeoPoint().getLatitude()+"\n longitude: " + matchLocation.getGeoPoint().getLatitude());
                    } else {
                        Log.d("333 TAG", "savedUserLocation: \ninsered user location into database." + "\n latitude: " + matchLocation.getGeoPoint().getLatitude()+"\n longitude: " + matchLocation.getGeoPoint().getLatitude());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
        } else {
        }
    }

    private void updateUserInFirestore() {

        if(matchModel != null) {
            db = FirebaseFirestore.getInstance();
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update("matches", FieldValue.arrayUnion(matchModel)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Match added..", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
        }
    }

    private static final int MAX_LENGTH = 12;

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
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

        String uid = random();

        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("matchImages")
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
                        setMatchProfileUrl(uri);
                    }
                });
    }

    private void setMatchProfileUrl(Uri uri) {

        pickedImageUri = uri;

        if(matchModel == null)
            matchModel = new MatchModel();

        if(pickedImageUri != null)
            matchModel.setImage_url(pickedImageUri.toString());
        else if(uri != null)
            matchModel.setImage_url(uri.toString());

        Glide.with(this).load(uri).into(match_image_AddMatch);
    }

    protected void setUpFont(){

        //region PickUpViews
        TextView txt_Add_Match = findViewById(R.id.txt_Add_Match);
        TextView txt_Add_MatchInfo = findViewById(R.id.txt_Add_MatchInfo);
        TextView txt_Add_MatchInfoExp = findViewById(R.id.txt_Add_MatchInfoExp);
        EditText etxt_Name_AddMatch = findViewById(R.id.etxt_Name_AddMatch);
        TextView txt_Date_AddMatch = findViewById(R.id.txt_Date_AddMatch);
        TextView txt_Upload_Image_AddMatch = findViewById(R.id.txt_Upload_Image_AddMatch);
        //endregion

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        txt_Add_Match.setTypeface(typeface);
        txt_Add_MatchInfo.setTypeface(typeface);
        txt_Upload_Image_AddMatch.setTypeface(typeface);


        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
        txt_Add_MatchInfoExp.setTypeface(typeface2);
        etxt_Name_AddMatch.setTypeface(typeface2);
        txt_Date_AddMatch.setTypeface(typeface2);
        //endregion
    }

}
