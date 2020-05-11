package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MatchActivity extends AppCompatActivity {

    String match_id;
    FirebaseFirestore db;
    MatchModel matchModel;

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

        if(match_id != null)
            setMatchInfo(match_id);

        setUpFont();
    }

    private void setMatchInfo(String match_id) {

        db = FirebaseFirestore.getInstance();

        db.collection("Matches").document(match_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    matchModel = task.getResult().toObject(MatchModel.class);
                    setUpInfo(matchModel);
                }
            }
        });
    }

    private void setUpInfo(MatchModel matchModel) {

        ImageView img_Match_Image = findViewById(R.id.img_Match_Image);

        if(matchModel.getImage_url().length() > 0)
            img_Match_Image.setImageURI(Uri.parse(matchModel.getImage_url()));
        else
            img_Match_Image.setImageURI(Uri.parse("https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a"));

        RelativeLayout rel_Subscribe = findViewById(R.id.rel_Subscribe);

        if(FirebaseAuth.getInstance().getUid() == matchModel.getCreator().getUser_id())
            rel_Subscribe.setVisibility(View.GONE);
        else rel_Subscribe.setVisibility(View.VISIBLE);

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
