package com.example.mosis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignUpMail extends AppCompatActivity {

    TextView textView;
    TextView textView2;
    Button btnNext;
    EditText infoTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_mail);

        setUpFont();

        //region SwitchToSignUp
        TextView txtSignUp = (TextView) findViewById(R.id.loginL);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_act2 = new Intent (SignUpMail.this, SignInActivity.class);
                startActivity(intent_act2);
                overridePendingTransition(0, 0);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail = infoTxt.getText().toString().trim();

                if(TextUtils.isEmpty(mail)){

                    infoTxt.setError("Email is Required");
                    return;
                }
                else {
                    //FIREBASE handler
                    goToIntent(mail);
                }
            }
        });
        //endregion

    }

    protected void setUpFont(){
        //region FontSetUp

        //set up fonta za sign in
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        textView = (TextView) findViewById(R.id.loginL);
        textView.setTypeface(typeface);

        //set up fonta za sign up
        textView2 = (TextView) findViewById(R.id.registerL);
        textView2.setTypeface(typeface);

        //set up fonta za button Submit
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        infoTxt = (EditText) findViewById(R.id.txtMail);
        infoTxt.setTypeface(typeface2);
        //endregion
    }

    void goToIntent(String email){
        Intent intent = new Intent( SignUpMail.this, SignUpPassword.class);
        intent.putExtra("Email", email);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
