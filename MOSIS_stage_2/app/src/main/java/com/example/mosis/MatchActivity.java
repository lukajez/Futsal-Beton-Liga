package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;

public class MatchActivity extends AppCompatActivity {

    String match_id;
    FirebaseFirestore db;
    MatchModel matchModel;
    Button btn_Subscribe_Match;
    private final int SUBSCRIPTION_POINTS = 2;
    RelativeLayout rel_Subscribe;
    private int SUBSCRIPTION_STATUS = -1;
    Button btn_Update_Match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        //region Handler for PutExtra
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
        //endregion

        if(match_id != null)
            setMatchInfo(match_id);

        btn_Subscribe_Match = findViewById(R.id.btn_Subscribe_Match);
        btn_Subscribe_Match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkForSubAndSub(matchModel);
            }
        });

        rel_Subscribe = findViewById(R.id.rel_Subscribe);
        rel_Subscribe.setVisibility(View.GONE);

        btn_Update_Match = findViewById(R.id.btn_Update_Match);

        btn_Update_Match.setVisibility(View.GONE);

        btn_Update_Match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MatchActivity.this, UpdateMatchActivity.class);
                intent.putExtra("match_id", matchModel.getId());
                startActivity(intent);
            }
        });

        setUpFont();
    }

    private ArrayList<MatchModel> subscriptions = new ArrayList<>();

    private void subscribeToMatch(final MatchModel matchModel) {

        btn_Subscribe_Match = findViewById(R.id.btn_Subscribe_Match);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    UserModel userModel = task.getResult().toObject(UserModel.class);
                    subscriptions = userModel.getSubscriptions();

                    for(MatchModel matchModel1 : subscriptions) {
                        if(matchModel1.getId().equals(matchModel.getId())) {
                            btn_Subscribe_Match.setText("You're Subscribed");
                            btn_Subscribe_Match.setEnabled(false);
                        }
                    }
                }
            }
        });
    }

    private UserModel currUser;
    private void checkForSubAndSub(final MatchModel matchModel) {

        currUser = new UserModel();

        db = FirebaseFirestore.getInstance();

        //TODO -- Update partitipiants -- add AuthUser to that array
        //db.collection("Matches").document(matchModel.getId()).update("partitipiants", FieldValue.arrayUnion())

        DocumentReference documentReference = db.collection("users").document(FirebaseAuth.getInstance().getUid());

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    currUser = task.getResult().toObject(UserModel.class);

                    addCurrentUserAsPartitipiant(currUser, matchModel.getId());
                }
            }
        });


        documentReference.update("subscriptions", FieldValue.arrayUnion(matchModel)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Subscribed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });

        documentReference.update("points", FieldValue.increment(SUBSCRIPTION_POINTS)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getStackTrace();
            }
        });

        btn_Subscribe_Match.setText("You're Subscribed");
        btn_Subscribe_Match.setEnabled(false);
    }

    private void addCurrentUserAsPartitipiant(final UserModel currUser, String id) {

        db = FirebaseFirestore.getInstance();

        db.collection("Matches").document(id).update("partitipiants", FieldValue.arrayUnion(currUser)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("TAG", "partitipiant: " + currUser);
            }
        });
    }

    private void setMatchInfo(String match_id) {

        db = FirebaseFirestore.getInstance();

        db.collection("Matches").document(match_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    matchModel = task.getResult().toObject(MatchModel.class);
                    setUpInfo(matchModel);
                    subscribeToMatch(matchModel);
                }
            }
        });
    }

    private void setUpInfo(MatchModel matchModel) {

        ImageView img_Match_Image = findViewById(R.id.img_Match_Image);

        if(matchModel.getImage_url().length() > 0) {
            Uri uri = Uri.parse(matchModel.getImage_url());
            img_Match_Image.setImageURI(null);
            img_Match_Image.setImageURI(uri);
            Glide.with(this).load(uri).into(img_Match_Image);

        } else {
            Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a");
            img_Match_Image.setImageURI(null);
            img_Match_Image.setImageURI(uri);
            Glide.with(this).load(uri).into(img_Match_Image);
        }

        rel_Subscribe = findViewById(R.id.rel_Subscribe);
        btn_Update_Match = findViewById(R.id.btn_Update_Match);

        if(FirebaseAuth.getInstance().getUid().equals(matchModel.getCreator().getUser_id())) {
            rel_Subscribe.setVisibility(View.GONE);
            btn_Update_Match.setVisibility(View.VISIBLE);
        } else {
            rel_Subscribe.setVisibility(View.VISIBLE);
            btn_Update_Match.setVisibility(View.GONE);
        };

        TextView txt_MatchNameInfo_Match = (TextView) findViewById(R.id.txt_MatchNameInfo_Match);
        txt_MatchNameInfo_Match.setText(matchModel.getName());

        TextView txt_MatchCreatorInfo_Match = (TextView) findViewById(R.id.txt_MatchCreatorInfo_Match);
        txt_MatchCreatorInfo_Match.setText(matchModel.getCreator().getUsername());

        TextView txt_TypeInfo_Match = (TextView) findViewById(R.id.txt_TypeInfo_Match);
        txt_TypeInfo_Match.setText(matchModel.getType());

        TextView txt_DateInfo_Match = (TextView) findViewById(R.id.txt_DateInfo_Match);
        txt_DateInfo_Match.setText(matchModel.getDate());

        TextView txt_StatusInfo_Match = findViewById(R.id.txt_StatusInfo_Match);
        txt_StatusInfo_Match.setText(matchModel.getStatus());

        if(matchModel.getStatus().equals("available"))
            txt_StatusInfo_Match.setTextColor(Color.GREEN);
        else
            txt_StatusInfo_Match.setTextColor(Color.RED);

    }

    private void setUpFont() {

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");

        TextView txt_Match = (TextView) findViewById(R.id.txt_Match);
        txt_Match.setTypeface(typeface);

        TextView txt_Add_MatchInfo = (TextView) findViewById(R.id.txt_Add_MatchInfo);
        txt_Add_MatchInfo.setTypeface(typeface);

        TextView txt_MatchNameHint_Match = (TextView) findViewById(R.id.txt_MatchNameHint_Match);
        txt_MatchNameHint_Match.setTypeface(typeface);

        TextView txt_MatchCreatorHint_Match = (TextView) findViewById(R.id.txt_MatchCreatorHint_Match);
        txt_MatchCreatorHint_Match.setTypeface(typeface);

        TextView txt_TypeHint_Match = (TextView) findViewById(R.id.txt_TypeHint_Match);
        txt_TypeHint_Match.setTypeface(typeface);

        TextView txt_DateHint_Match = (TextView) findViewById(R.id.txt_DateHint_Match);
        txt_DateHint_Match.setTypeface(typeface);

        TextView txt_SubHint_Match = (TextView) findViewById(R.id.txt_SubHint_Match);
        txt_SubHint_Match.setTypeface(typeface);

        Button btn_Subscribe_Match = findViewById(R.id.btn_Subscribe_Match);
        btn_Subscribe_Match.setTypeface(typeface);

        btn_Update_Match = findViewById(R.id.btn_Update_Match);
        btn_Update_Match.setTypeface(typeface);

        TextView txt_StatusHint_Match = findViewById(R.id.txt_StatusHint_Match);
        txt_StatusHint_Match.setTypeface(typeface);


        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        TextView txt_Add_MatchInfoExp = (TextView) findViewById(R.id.txt_Add_MatchInfoExp);
        txt_Add_MatchInfoExp.setTypeface(typeface2);

        TextView txt_MatchNameInfo_Match = (TextView) findViewById(R.id.txt_MatchNameInfo_Match);
        txt_MatchNameInfo_Match.setTypeface(typeface2);

        TextView txt_MatchCreatorInfo_Match = (TextView) findViewById(R.id.txt_MatchCreatorInfo_Match);
        txt_MatchCreatorInfo_Match.setTypeface(typeface2);

        TextView txt_TypeInfo_Match = (TextView) findViewById(R.id.txt_TypeInfo_Match);
        txt_TypeInfo_Match.setTypeface(typeface2);

        TextView txt_DateInfo_Match = (TextView) findViewById(R.id.txt_DateInfo_Match);
        txt_DateInfo_Match.setTypeface(typeface2);

        TextView txt_SubInfo_Match = (TextView) findViewById(R.id.txt_SubInfo_Match);
        txt_SubInfo_Match.setTypeface(typeface2);

        TextView txt_StatusInfo_Match = (TextView) findViewById(R.id.txt_StatusInfo_Match);
        txt_StatusInfo_Match.setTypeface(typeface2);
    }
}
