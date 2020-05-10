package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class AddMatchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView txtDateAddMatch;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Button btn_Confirm_AddMatch;
    private EditText etxt_Name_AddMatch;

    private boolean fieldName = false;
    private boolean fieldType = true;
    private boolean fieldDate = false;
    private String matchId = "";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private MatchLocation matchLocation;
    private MatchModel matchModel = null;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_match);

        txtDateAddMatch = (TextView) findViewById(R.id.txt_Date_AddMatch);
        etxt_Name_AddMatch = findViewById(R.id.etxt_Name_AddMatch);
        btn_Confirm_AddMatch = findViewById(R.id.btn_Confirm_AddMatch);

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

        setUpFont();
    }

    private void addMatchToFirebase() {
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

        if(matchModel == null) {

            matchModel = new MatchModel();

            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()) {
                        userModel = task.getResult().toObject(UserModel.class);

                        matchModel.setCreator(userModel);
                        matchModel.setName(name);
                        matchModel.setType(type);
                        matchModel.setDate(date);

                        setMatchInFirestore();
                        //updateUserInFirestore();
                    }
                }
            });
        }
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
                    overridePendingTransition(0, 0);

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
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.getStackTrace();
                }
            });
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





    private void getUserForMatch() {

        db = FirebaseFirestore.getInstance();

        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    UserModel user = Objects.requireNonNull(task.getResult()).toObject(UserModel.class);
//                    matchLocation.setUserModel(user);
//                    saveMatchLocation();
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

        db = FirebaseFirestore.getInstance();

        if(matchLocation != null){

            DocumentReference locationRef = db.collection("Match Locations").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

            locationRef.set(matchLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TAG", "savedMatchLocation: \ninserted match location into database." + "\n latitude: " + matchLocation.getGeoPoint().getLatitude()+"\n longitude: " + matchLocation.getGeoPoint().getLatitude());
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
