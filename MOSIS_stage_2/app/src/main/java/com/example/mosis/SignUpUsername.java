package com.example.mosis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

public class SignUpUsername extends AppCompatActivity {

    String email, userID;
    EditText SignUpUname;
    Button btnNext;
    TextView textView;
    TextView textView2;
    Button btnBack;
    EditText infoTxt;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_username);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        }

        setUpFont();

        //region Handler za PutExtra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                email = null;
                userID = null;
                user = null;
            } else {
                email = extras.getString("Email");
                userID = extras.getString("UserID");
            }
        } else {
            email = (String) savedInstanceState.getSerializable("Email");
            userID = (String) savedInstanceState.getSerializable("UserID");
        }

        //provera za Email da li ga prenosi
        Log.d("TAG", "Persons EMAIL is: " + email);
        Log.d("TAG", "Persons USERID is: " + userID);
        //endregion

        SignUpUname = findViewById(R.id.txtUsername);
        btnNext = (Button) findViewById(R.id.btnNextUsername);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = infoTxt.getText().toString().trim();

                if(TextUtils.isEmpty(username)){

                    infoTxt.setError("Username is Required");
                    return;
                }
                else {
                    goToActivity(username);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( SignUpUsername.this, SignUpPassword.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    private void goToActivity(String username) {
        Intent intent = new Intent( SignUpUsername.this, SignUpTeam.class);
        intent.putExtra("Email", email);
        intent.putExtra("UserID", userID);
        intent.putExtra("Username", username);
        startActivity(intent);
        overridePendingTransition(0, 0);
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
        btnNext = (Button) findViewById(R.id.btnNextUsername);
        btnNext.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        infoTxt = (EditText) findViewById(R.id.txtUsername);
        infoTxt.setTypeface(typeface2);

        btnBack = (Button) findViewById(R.id.btnBackTeam);
        btnBack.setTypeface(typeface);
        //endregion
    }
}
