package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpTeam extends AppCompatActivity {

    String email, userID, username;
    EditText SignUpTeam;
    Button btnSubmitTeam;
    TextView textView;
    TextView textView2;
    Button btnBack;
    EditText infoTxt;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_team);

        setUpFont();


        //region Handler za PutExtra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                email = null;
                userID = null;
                username = null;
            } else {
                email = extras.getString("Email");
                userID = extras.getString("UserID");
                username = extras.getString("Username");
            }
        } else {
            email = (String) savedInstanceState.getSerializable("Email");
            userID = (String) savedInstanceState.getSerializable("UserID");
            username = (String) savedInstanceState.getSerializable("Username");
        }

        //provera za Email da li ga prenosi
        Log.d("TAG", "Persons EMAIL is: " + email);
        Log.d("TAG", "Persons USERID is: " + userID);
        Log.d("TAG", "Persons USERNAME is: " + username);
        //endregion


        btnSubmitTeam = (Button) findViewById(R.id.btnSubmitTeam);
        SignUpTeam = (EditText) findViewById(R.id.txtTeam);
        fStore = FirebaseFirestore.getInstance();

        btnSubmitTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String team = SignUpTeam.getText().toString();

                if(TextUtils.isEmpty(team)){
                    Toast.makeText(getApplicationContext(),"Please enter your Team name",Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    DocumentReference documentReference = fStore.collection("users").document(userID);
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("username", username);
                    user.put("team", team);

                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    Intent intent = new Intent(SignUpTeam.this, SignUpPhoto.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }
        });




        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( SignUpTeam.this, SignUpUsername.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    void setUpFont(){
        //region FontSetUp

        //set up fonta za sign in
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        textView = (TextView) findViewById(R.id.loginL);
        textView.setTypeface(typeface);

        //set up fonta za sign up
        textView2 = (TextView) findViewById(R.id.registerL);
        textView2.setTypeface(typeface);

        //set up fonta za button Submit
        btnSubmitTeam = (Button) findViewById(R.id.btnSubmitTeam);
        btnSubmitTeam.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        infoTxt = (EditText) findViewById(R.id.txtTeam);
        infoTxt.setTypeface(typeface2);

        btnBack = (Button) findViewById(R.id.btnBackTeam);
        btnBack.setTypeface(typeface);
        //endregion
    }
}
