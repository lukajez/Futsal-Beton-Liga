package com.example.mosis;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {

    RecyclerView recyclerView;
    EditText searchField;
    String searchTerm;
    ImageButton searchButton;
    FirebaseFirestore db;
    List<User> userList = new ArrayList<>();
    RecyclerView.LayoutManager layoutManager;
    CustomAdapter adapter;
    View view;
    TextView txt_Search, txt_Search_Hint, txt_Search_HintExp;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_search, container, false);


        searchField = (EditText) view.findViewById(R.id.searchField);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_search);
        searchButton = (ImageButton) view.findViewById(R.id.searchButton);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        if(searchField.getText().toString().length() < 1) {
            showData();
        }
        setUpFont();



        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){
                searchTerm = s.toString();
                searchUserByTerm(searchTerm);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });

        return view;
    }

    private void showData() {
        db = FirebaseFirestore.getInstance();

        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {

                    for(DocumentSnapshot documentSnapshot : task.getResult()) {

                        Log.d("TAG", documentSnapshot.get("username").toString() + " " + documentSnapshot.get("team").toString());
                        User user = new User((String) documentSnapshot.get("username").toString(), (String) documentSnapshot.get("team").toString(), (String) documentSnapshot.get("points").toString(), (String) documentSnapshot.get("image_url"));
                        userList.add(user);
                    }

                    adapter = new CustomAdapter(getContext(), userList);
                    recyclerView.setAdapter(adapter);
                }
            }
        });
    }

    protected  void searchUser() {
    }

    protected  void searchUserByTerm(String searchTerm) {

        db = FirebaseFirestore.getInstance();
        if(searchTerm.length() > 0) {

            db.collection("users").whereEqualTo("username", searchTerm.toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        userList.clear();
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {
                            Log.d("TAG", documentSnapshot.get("username").toString() + " " + documentSnapshot.get("team").toString());
                            User user = new User((String) documentSnapshot.get("username").toString(), (String) documentSnapshot.get("team").toString(), (String) documentSnapshot.get("points").toString(), (String) documentSnapshot.get("image_url"));
                            userList.add(user);
                        }

                        adapter = new CustomAdapter(getContext(), userList);
                        recyclerView.setAdapter(adapter);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        } else {
            showData();
        }
    }

    protected void setUpFont(){

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        txt_Search = (TextView) view.findViewById(R.id.txt_Search);
        txt_Search.setTypeface(typeface);

        txt_Search_Hint = (TextView) view.findViewById(R.id.txt_Search_Hint);
        txt_Search_Hint.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");
        searchField = (EditText) view.findViewById(R.id.searchField);
        searchField.setTypeface(typeface2);

        txt_Search_HintExp = (TextView) view.findViewById(R.id.txt_Search_HintExp);
        txt_Search_HintExp.setTypeface(typeface2);
        //endregion
    }

}
