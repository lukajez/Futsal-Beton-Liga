package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;


public class SignUpPassword extends AppCompatActivity {

    String email, userID;
    EditText SignUpPass;
    Button btnNext;
    Button btnBack;
    private FirebaseAuth auth;
    //FireStore


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_password);

        setUpFont();

        //region Handler za PutExtra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                email = null;
            } else {
                email = extras.getString("Email");
            }
        } else {
            email = (String) savedInstanceState.getSerializable("Email");
        }

        //provera za Email da li ga prenosi
        Log.d("TAG", "Persons EMAIL is: " + email);
        //endregion

        SignUpPass = findViewById(R.id.txtPassword);
        btnNext = (Button) findViewById(R.id.btnNextPassword);

        auth=FirebaseAuth.getInstance();
        //fStore = FirebaseFirestore.getInstance();

        btnNext.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                final String pass = SignUpPass.getText().toString();


                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(getApplicationContext(),"Please enter your Password",Toast.LENGTH_LONG).show();
                }
                if (pass.length() == 0){
                    Toast.makeText(getApplicationContext(),"Please enter your Password",Toast.LENGTH_LONG).show();
                }
                if (pass.length()<8){
                    Toast.makeText(getApplicationContext(),"Password must be more than 8 digit",Toast.LENGTH_LONG).show();
                }
                else{
                    auth.createUserWithEmailAndPassword(email,pass)
                            .addOnCompleteListener(SignUpPassword.this, new OnCompleteListener<AuthResult>() {
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                        userID = auth.getCurrentUser().getUid();

                                        goToActivity();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });}
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( SignUpPassword.this, SignUpMail.class);
                //TODO: Povratni data da se posalje ka Activity-u
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

    }

    protected void setUpFont(){
        //region FontSetUp

        //set up fonta za sign in
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        TextView textView = (TextView) findViewById(R.id.loginL);
        textView.setTypeface(typeface);

        //set up fonta za sign up
        TextView textView2 = (TextView) findViewById(R.id.registerL);
        textView2.setTypeface(typeface);

        //set up fonta za button Submit
        btnNext = (Button) findViewById(R.id.btnNextPassword);
        btnNext.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        EditText infoTxt = (EditText) findViewById(R.id.txtPassword);
        infoTxt.setTypeface(typeface2);

        btnBack = (Button) findViewById(R.id.btnBackTeam);
        btnBack.setTypeface(typeface);
        //endregion
    }

    void goToActivity(){
        Intent intent = new Intent( SignUpPassword.this, SignUpUsername.class);
        intent.putExtra("Email", email);
        intent.putExtra("UserID", userID);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}
