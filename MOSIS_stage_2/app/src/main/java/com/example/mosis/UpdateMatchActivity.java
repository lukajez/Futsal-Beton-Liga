package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.printservice.CustomPrinterIconCallback;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class UpdateMatchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private ArrayList<UserModel> userModels = new ArrayList<>();
    private ArrayList<String> friends = new ArrayList<>();
    private CustomAdapter adapter;
    private String match_id;
    private Spinner spinner_UpdateMatch;
    private Button btn_Confirm_UpdateMatch;
    private String potentialWinner;
    private int WINNING_POINTS;
    private MatchModel matchModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_match);



        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                match_id = null;

            } else {
                match_id = extras.getString("match_id");
            }
        } else {
            match_id = (String) savedInstanceState.getSerializable("match_id");
        }

        Log.d("62 TAG: ", "match_id: " + match_id);
        if(match_id != null) {
            Log.d("64 TAG: ", "match_id: " + match_id);
            setUpPartitipiants(match_id);
        }

        btn_Confirm_UpdateMatch = findViewById(R.id.btn_Confirm_UpdateMatch);

        btn_Confirm_UpdateMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setUpWinner();

                try {
                    Intent intent = new Intent(UpdateMatchActivity.this, HomeActivity.class);
                    startActivity(intent);
                } finally {
                    finish();
                }
            }
        });

        setUpFont();
    }

    private UserModel winnerUser;
    private void setUpWinner() {

        winnerUser = new UserModel();

        db = FirebaseFirestore.getInstance();

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    for(DocumentSnapshot documentSnapshot : task.getResult()) {
                        if(documentSnapshot.toObject(UserModel.class).getUsername().equals(potentialWinner)) {
                            winnerUser = documentSnapshot.toObject(UserModel.class);

                            if(winnerUser != null) {
                                setUpWinnerUserFirestore(winnerUser);
                            }
                        }
                    }
                }
            }
        });
    }

    private void setUpWinnerUserFirestore(final UserModel winnerUser) {

        db = FirebaseFirestore.getInstance();

        db.collection("Matches").document(match_id).update("winner", winnerUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("110 TAG", "winnerUser: " + winnerUser);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });

        db.collection("users").document(winnerUser.getUser_id()).update("points", FieldValue.increment(WINNING_POINTS));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT);
        potentialWinner = text;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private ArrayList<UserModel> partitipiants = new ArrayList<>();

    private void setUpPartitipiants(String match_id) {

        db = FirebaseFirestore.getInstance();

        db.collection("Matches").document(match_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    matchModel = Objects.requireNonNull(task.getResult()).toObject(MatchModel.class);
                    switch (matchModel.getType()) {
                        case "Single Match" :
                            WINNING_POINTS = 3;
                            break;
                        case "Tournament" :
                            WINNING_POINTS = 10;
                            break;
                        case "Charity Tournament" :
                            WINNING_POINTS = 10;
                            break;
                        case "Friendly Tournament" :
                            WINNING_POINTS = 6;
                            break;
                    }
                    assert matchModel != null;
                    partitipiants = Objects.requireNonNull(matchModel.getPartitipiants());

                    Log.d("91 TAG: ", "partitipiants: " + partitipiants);
                    setUpSpinner(partitipiants);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });
    }

    private void setUpSpinner(ArrayList<UserModel> partitipiants) {

        spinner_UpdateMatch = findViewById(R.id.spinner_UpdateMatch);

        ArrayList<String> _partitipiants = new ArrayList<>();

        for(UserModel userModel : partitipiants) {

            _partitipiants.add(userModel.getUsername());

        }

        Log.d("114 TAG: ", "_partitipiants: " + _partitipiants);

        MySpinnerAdapter adapter = new MySpinnerAdapter(
                this,
                android.R.layout.simple_spinner_item,
                _partitipiants
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner_UpdateMatch.setAdapter(adapter);
        spinner_UpdateMatch.setOnItemSelectedListener(this);
    }

    private void setUpFont() {

        TextView txt_Update_MatchInfoExp = findViewById(R.id.txt_Update_MatchInfoExp);
        TextView txt_Update_MatchInfo = findViewById(R.id.txt_Update_MatchInfo);
        TextView txt_Update_Match = findViewById(R.id.txt_Update_Match);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        txt_Update_MatchInfo.setTypeface(typeface);
        txt_Update_Match.setTypeface(typeface);

        Typeface typeface1 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
        txt_Update_MatchInfoExp.setTypeface(typeface1);
    }
}
