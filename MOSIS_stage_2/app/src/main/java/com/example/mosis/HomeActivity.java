package com.example.mosis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.firebase.auth.FirebaseAuth;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class HomeActivity extends AppCompatActivity {

    ChipNavigationBar bottomNav;
    FragmentManager fragmentManager;
    TextView txt_pomocni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));


        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        bottomNav = findViewById(R.id.bottom_nav);

        if(savedInstanceState == null) {
            bottomNav.setItemSelected(R.id.map, true);
            fragmentManager = getSupportFragmentManager();
            MapFragment mapFragment = new MapFragment();
            fragmentManager.beginTransaction().replace(R.id.fragment_container, mapFragment).commit();
        }

        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i) {

                    case R.id.map:
                        fragment = new MapFragment();
                        break;
                    case R.id.request:
                        fragment = new RequestFragment();
                        break;
                    case R.id.search:
                        fragment = new SearchFragment();
                        break;
                    case R.id.menu_profile:
                        fragment = new ProfileFragment();
                        break;
                }

                if(fragment != null) {
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
                } else {
                    Log.e("TAG", "ERROR");
                }
            }
        });



//        final StorageReference reference = FirebaseStorage.getInstance().getReference()
//                .child("profileImages")
//                .child(uid + ".jpeg");
//
//        imageView = (ImageView) findViewById(R.id.imageViewID);
//
//        getDownloadUrl(reference, imageView);
    }

//    private void getDownloadUrl(StorageReference reference, final ImageView img) {
//        reference.getDownloadUrl()
//                .addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        Log.d("TAG", "onSuccess DOWNLOAD URL IS: " + uri);
//                        setUserProfileUrl(uri, img);
//                    }
//                });
//    }

//    private void setUserProfileUrl(Uri uri, ImageView img) {
//
//        Glide.with(this).load(uri).into(img);
//    }

//    protected void setUpFont(){
//        //region FontSetUp
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
//        textView = (TextView) findViewById(R.id.loginL);
//        textView.setTypeface(typeface);
//
//
//        textView2 = (TextView) findViewById(R.id.registerL);
//        textView2.setTypeface(typeface);
//
//
//        btnSubmit = (Button) findViewById(R.id.btnSubmit);
//        btnSubmit.setTypeface(typeface);
//
//        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
//
//        EditText infoTxt = (EditText) findViewById(R.id.txtEmail);
//        infoTxt.setTypeface(typeface2);
//
//        EditText txtPassword = (EditText) findViewById(R.id.txtPassword);
//        txtPassword.setTypeface(typeface2);
//        //endregion
//    }
}
