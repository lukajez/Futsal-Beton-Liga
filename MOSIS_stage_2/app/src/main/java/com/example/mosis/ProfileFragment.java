package com.example.mosis;

import android.content.Intent;

import android.graphics.Typeface;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Objects;


public class ProfileFragment extends Fragment {

    private TextView txtUsername;
    private TextView txtTeam;
    private TextView txtPoints;
    private View view;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ImageView imageView;
    private Uri pickedImage;

    private RecyclerView recycler_friends_profile;

    private CustomAdapter adapter;
    private ArrayList<String> friends;
    private ArrayList<User> friendsList;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageButton btnAddFriendRequest = view.findViewById(R.id.btnAddFriendRequest);
        ImageButton btnRangList = view.findViewById(R.id.btnRangList);
        recycler_friends_profile = view.findViewById(R.id.recycler_friends_profile);
        recycler_friends_profile.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_friends_profile.setLayoutManager(layoutManager);

        friends = new ArrayList<>();
        friendsList = new ArrayList<>();

        btnAddFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new Intent(getActivity(), BluetoothActivity.class);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).overridePendingTransition(0,0);

                } finally {
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        });

        btnRangList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getActivity(), RangListActivity.class);
                    startActivity(intent);
                    Objects.requireNonNull(getActivity()).overridePendingTransition(0,0);

                } finally {
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        });

        this.getMyFriends();
        this.setUpInfo();
        this.setUpFont();
        return view;
    }

    private void getMyFriends() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uuid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        db = FirebaseFirestore.getInstance();

        db.collection("users").document(uuid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                friends = (ArrayList<String>) documentSnapshot.get("friends");
                Log.d("FRIENDS", "THESE ARE FRIENDS: " + friends);
                setUpFriends(friends);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void setUpFriends(ArrayList<String> friends) {

        db = FirebaseFirestore.getInstance();

        for(String fr : friends) {

            db.collection("users").whereEqualTo("user_id", fr).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {

                            Log.d("TAG SET UP FRIENDS", Objects.requireNonNull(documentSnapshot.get("username")).toString() + " " + Objects.requireNonNull(documentSnapshot.get("team")).toString());
                            User user = new User(Objects.requireNonNull(documentSnapshot.get("username")).toString(), Objects.requireNonNull(documentSnapshot.get("team")).toString(), Objects.requireNonNull(documentSnapshot.get("points")).toString(), Objects.requireNonNull(documentSnapshot.get("image_url")).toString());
                            friendsList.add(user);
                        }

                        adapter = new CustomAdapter(getContext(), friendsList);
                        recycler_friends_profile.setAdapter(adapter);
                    }
                }
            });

//            db.collection("users").document(fr).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                    User users = new User((String) documentSnapshot.get("username").toString(), (String) documentSnapshot.get("team").toString(), (String) documentSnapshot.get("points").toString(), (String) documentSnapshot.get("image_url"));
//                    friendsList.add(users);
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                }
//            });
        }

        Log.d("FRIENDS LIST", "THESE ARE YOUR FRIENDS: " + friendsList);
    }

    private void setUpInfo() {
        txtUsername = view.findViewById(R.id.txt_profile_username);
        txtTeam =  view.findViewById(R.id.txt_profile_team);
        imageView = view.findViewById(R.id.img_profile_icon);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;

        db.collection("users").whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                if(Objects.requireNonNull(documentSnapshot.get("image_url")).toString().length() > 0)
                                    pickedImage = Uri.parse(Objects.requireNonNull(documentSnapshot.get("image_url")).toString());

                                Log.d("TAG", "onSuccess DOWNLOAD URL IS: " + Objects.requireNonNull(documentSnapshot.get("image_url")).toString());
                                txtUsername.setText(Objects.requireNonNull(documentSnapshot.get("username")).toString());
                                txtTeam.setText(Objects.requireNonNull(documentSnapshot.get("team")).toString());
                                txtPoints.setText(Objects.requireNonNull(documentSnapshot.get("points")).toString());
                                Log.d("TAG", documentSnapshot.get("username") + " " + documentSnapshot.get("team") + " " + user);
                            }
                            if(pickedImage != null){
                                setUserProfileUrl(pickedImage, imageView);
                            }
                        }
                    }
                });



//        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(uid + ".jpeg");
//        imageView = (ImageView) view.findViewById(R.id.img_profile_icon);

    }

    private void setUserProfileUrl(Uri uri, ImageView img) {
        Log.d("SET USER PROFILE URL FUN", "onSuccess DOWNLOAD URL IS: " + uri);

        Glide.with(this).load(uri).into(img);
    }

    protected void setUpFont(){

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(Objects.requireNonNull(getContext()).getAssets(), "fonts/bebasneue.ttf");
        txtUsername = view.findViewById(R.id.txt_profile_username);
        txtUsername.setTypeface(typeface);

        txtTeam = view.findViewById(R.id.txt_profile_team);
        txtTeam.setTypeface(typeface);

        txtPoints =view.findViewById(R.id.txt_profile_points);
        txtPoints.setTypeface(typeface);


        TextView txt_profile_my_matches = view.findViewById(R.id.txt_profile_my_matches);
        txt_profile_my_matches.setTypeface(typeface);

        TextView txt_profile_my_friends = view.findViewById(R.id.txt_profile_my_friends);
        txt_profile_my_friends.setTypeface(typeface);

        TextView txt_Profile = view.findViewById(R.id.txt_Profile);
        txt_Profile.setTypeface(typeface);


        Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");

        TextView txt_profile_my_friendsExp = view.findViewById(R.id.txt_profile_my_friendsExp);
        txt_profile_my_friendsExp.setTypeface(typeface2);

        TextView txt_profile_my_matchesExp = view.findViewById(R.id.txt_profile_my_matchesExp);
        txt_profile_my_matchesExp.setTypeface(typeface2);
        //endregion
    }
}
