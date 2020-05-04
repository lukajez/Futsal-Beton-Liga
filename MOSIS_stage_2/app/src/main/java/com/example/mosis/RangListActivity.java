package com.example.mosis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RangListActivity extends AppCompatActivity {

    RecyclerView recycler_RangList;
    Button btn_Back_RangList;
    TextView txt_Rang_List, txt_rangList_info, txt_rangList_infoExp;
    CustomAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    List<Users> usersList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_list);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccent));


        recycler_RangList = findViewById(R.id.recycler_RangList);
        btn_Back_RangList = findViewById(R.id.btn_Back_RangList);

        recycler_RangList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_RangList.setLayoutManager(layoutManager);

        btn_Back_RangList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(RangListActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } finally {
                    finish();
                }


            }
        });

        setUpData();
        setUpFont();
    }

    private void setUpData() {

        db = FirebaseFirestore.getInstance();

        db.collection("users").orderBy("points", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {

                    usersList.clear();

                    for(DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                        Log.d("TAG", Objects.requireNonNull(documentSnapshot.get("username")).toString() + " " + Objects.requireNonNull(documentSnapshot.get("team")).toString());
                        Users users = new Users(Objects.requireNonNull(documentSnapshot.get("username")).toString(), Objects.requireNonNull(documentSnapshot.get("team")).toString(), Objects.requireNonNull(documentSnapshot.get("points")).toString(), Objects.requireNonNull(documentSnapshot.get("image_url")).toString());
                        usersList.add(users);
                    }

                    adapter = new CustomAdapter(getApplicationContext(), usersList);
                    recycler_RangList.setAdapter(adapter);
                }
            }
        });
    }

    protected void setUpFont(){

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/bebasneue.ttf");
        txt_rangList_info = findViewById(R.id.txt_rangList_info);
        txt_rangList_info.setTypeface(typeface);

        txt_Rang_List = findViewById(R.id.txt_Rang_List);
        txt_Rang_List.setTypeface(typeface);

        btn_Back_RangList = findViewById(R.id.btn_Back_RangList);
        btn_Back_RangList.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getAssets(), "fonts/adventproregular.ttf");
        txt_rangList_infoExp = findViewById(R.id.txt_rangList_infoExp);
        txt_rangList_infoExp.setTypeface(typeface2);
        //endregion
    }
}
