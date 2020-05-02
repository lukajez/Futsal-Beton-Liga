package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    TextView textView;
    TextView textView2;
    Button btnSubmit;
    EditText SignInEmail;
    EditText SignInPassword;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
//        }

        setUpFont();

        SignInEmail = (EditText) findViewById(R.id.txtEmail);
        SignInPassword = (EditText) findViewById(R.id.txtPassword);
        fAuth = FirebaseAuth.getInstance();
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = SignInEmail.getText().toString().trim();
                String password = SignInPassword.getText().toString().trim();

                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignInActivity.this, "Welcome my favourite user", Toast.LENGTH_LONG).show();
                            Intent intent_act2 = new Intent (SignInActivity.this, HomeActivity.class);
                            startActivity(intent_act2);
                            overridePendingTransition(0, 0);
                        }
                        else
                        {
                            Toast.makeText(SignInActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        //region SwitchToSignUp
        TextView txtSignUp = (TextView) findViewById(R.id.registerL);
        txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity();
            }
        });
        //endregion
    }

    protected void setUpFont(){
        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        textView = (TextView) findViewById(R.id.loginL);
        textView.setTypeface(typeface);


        textView2 = (TextView) findViewById(R.id.registerL);
        textView2.setTypeface(typeface);


        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");

        EditText infoTxt = (EditText) findViewById(R.id.txtEmail);
        infoTxt.setTypeface(typeface2);

        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setTypeface(typeface2);
        //endregion
    }

    protected void goToActivity(){
        Intent intent_act2 = new Intent (SignInActivity.this, SignUpMail.class);
        startActivity(intent_act2);
        overridePendingTransition(0, 0);
    }
}
