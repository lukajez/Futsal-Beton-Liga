package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {

    TextView txtUsername;
    TextView txtTeam;
    TextView txtPoints;
    View view;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setUpFont();
    }

    protected void setUpInfo() {
        txtUsername = (TextView) view.findViewById(R.id.txt_profile_username);
        txtTeam = (TextView) view.findViewById(R.id.txt_profile_team);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        final String current = user.getUid();

        db.collection("users").whereEqualTo("uId", current)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                txtUsername.setText((CharSequence) documentSnapshot.get("username"));
                                txtTeam.setText((CharSequence) documentSnapshot.get("team"));
                                Log.d("TAG", documentSnapshot.get("username") + " " + documentSnapshot.get("team") + " " + user);
                            }
                        }
                    }
                });
    }

    protected void setUpFont(){
        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        txtUsername = (TextView) findViewById(R.id.txt_profile_username);
        txtUsername.setTypeface(typeface);


        txtTeam = (TextView) findViewById(R.id.txt_profile_team);
        txtTeam.setTypeface(typeface);

        txtPoints = (TextView) findViewById(R.id.txt_profile_points);
        txtPoints.setTypeface(typeface);
//
//        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
//
//        EditText infoTxt = (EditText) findViewById(R.id.txtEmail);
//        infoTxt.setTypeface(typeface2);
//
//        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
//        txtPassword.setTypeface(typeface2);
        //endregion
    }
}
