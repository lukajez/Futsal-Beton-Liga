package com.example.mosis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendActivity extends AppCompatActivity {

    String username, team, points, image;
    CircleImageView img_friend_profile_icon;
    TextView txt_friend_profile_username, txt_friend_profile_team, txt_friend_profile_points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));


        //region Handler za PutExtra
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                username = null;
                team = null;
                points = null;
                image = null;
            } else {
                username = extras.getString("username");
                team = extras.getString("team");
                points = extras.getString("points");
                image = extras.getString("image");
            }
        } else {
            username = (String) savedInstanceState.getSerializable("username");
            team = (String) savedInstanceState.getSerializable("team");
            points = (String) savedInstanceState.getSerializable("points");
            image = (String) savedInstanceState.getSerializable("image");
        }

        //provera za Email da li ga prenosi
        Log.d("TAG", "Persons USERNAME is: " + username);
        Log.d("TAG", "Persons TEAM is: " + team);
        Log.d("TAG", "Persons POINTS is: " + points);
        Log.d("TAG", "Persons IMAGE is: " + image);
        //endregion

        setUpInfo();


    }

    private void setUpInfo() {

        if(image.length() > 0) {
            img_friend_profile_icon = (CircleImageView) findViewById(R.id.img_friend_profile_icon);
            Uri uri = Uri.parse(image);
            Glide.with(this).load(uri).into(img_friend_profile_icon);
        }

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");

        txt_friend_profile_username = (TextView) findViewById(R.id.txt_friend_profile_username);
        txt_friend_profile_username.setText(username);
        txt_friend_profile_username.setTypeface(typeface);

        txt_friend_profile_team = (TextView) findViewById(R.id.txt_friend_profile_team);
        txt_friend_profile_team.setText(team);
        txt_friend_profile_team.setTypeface(typeface);

        txt_friend_profile_points = (TextView) findViewById(R.id.txt_friend_profile_points);
        txt_friend_profile_points.setText(points);
        txt_friend_profile_points.setTypeface(typeface);
    }


}
