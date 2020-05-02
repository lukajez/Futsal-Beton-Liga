package com.example.mosis;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ProfileFragment extends Fragment {

    TextView txtUsername, txt_addFriend_Profile, txtTeam, txtPoints, txt_profile_my_matches, txt_profile_my_friends, txt_profile_my_friendsExp, txt_profile_my_matchesExp, txt_RangList_Profile;
    View view;
    FirebaseFirestore db;
    FirebaseUser user;
    ImageView imageView;
    Uri pickedImage;
    ImageButton btnAddFriendRequest, btnRangList;
    RecyclerView recycler_friends_profile;
    CustomAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<String> friends;
    ArrayList<Users> friendsList;

    private static final int TAKE_IMAGE_CODE = 10001;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        btnAddFriendRequest = (ImageButton) view.findViewById(R.id.btnAddFriendRequest);
        btnRangList = (ImageButton) view.findViewById(R.id.btnRangList);
        recycler_friends_profile = (RecyclerView) view.findViewById(R.id.recycler_friends_profile);
        recycler_friends_profile.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recycler_friends_profile.setLayoutManager(layoutManager);

        friends = new ArrayList<>();
        friendsList = new ArrayList<>();

        btnAddFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Intent intent = new Intent(getActivity(), BluetoothActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0,0);

                } finally {
                    getActivity().finish();
                }
            }
        });

        btnRangList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(getActivity(), RangListActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(0,0);

                } finally {
                    getActivity().finish();
                }
            }
        });

        this.getMyFriends();
        this.setUpInfo();
        this.setUpFont();
        return view;
    }
//    public void handleImageClick(View view) {
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if(intent.resolveActivity(getContext().getPackageManager()) != null){
//            startActivityForResult(intent, TAKE_IMAGE_CODE);
//        }
//    }
//    @Override
//    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == TAKE_IMAGE_CODE) {
//            switch (resultCode){
//                case RESULT_OK:
//                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                    handleUpload(bitmap);
//            }
//        }
//    }

    private void getMyFriends() {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uuid = auth.getCurrentUser().getUid();

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
                        for(DocumentSnapshot documentSnapshot : task.getResult()) {

                            Log.d("TAG SET UP FRIENDS", documentSnapshot.get("username").toString() + " " + documentSnapshot.get("team").toString());
                            Users users = new Users((String) documentSnapshot.get("username").toString(), (String) documentSnapshot.get("team").toString(), (String) documentSnapshot.get("points").toString(), (String) documentSnapshot.get("image_url"));
                            friendsList.add(users);
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
//                    Users users = new Users((String) documentSnapshot.get("username").toString(), (String) documentSnapshot.get("team").toString(), (String) documentSnapshot.get("points").toString(), (String) documentSnapshot.get("image_url"));
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

    protected void setUpInfo() {
        txtUsername = (TextView) view.findViewById(R.id.txt_profile_username);
        txtTeam = (TextView) view.findViewById(R.id.txt_profile_team);
        imageView = (ImageView) view.findViewById(R.id.img_profile_icon);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        db.collection("users").whereEqualTo("email", user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for(DocumentSnapshot documentSnapshot : task.getResult()) {
                                if(documentSnapshot.get("image_url").toString().length() > 0)
                                    pickedImage = Uri.parse(documentSnapshot.get("image_url").toString());

                                Log.d("TAG", "onSuccess DOWNLOAD URL IS: " + documentSnapshot.get("image_url").toString());
                                txtUsername.setText((CharSequence) documentSnapshot.get("username"));
                                txtTeam.setText((CharSequence) documentSnapshot.get("team"));
                                txtPoints.setText((CharSequence) documentSnapshot.get("points").toString());
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
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/bebasneue.ttf");
        txtUsername = (TextView) view.findViewById(R.id.txt_profile_username);
        txtUsername.setTypeface(typeface);

        txtTeam = (TextView) view.findViewById(R.id.txt_profile_team);
        txtTeam.setTypeface(typeface);

        txtPoints = (TextView) view.findViewById(R.id.txt_profile_points);
        txtPoints.setTypeface(typeface);

        txt_addFriend_Profile = (TextView) view.findViewById(R.id.txt_addFriend_Profile);
        txt_addFriend_Profile.setTypeface(typeface);

        txt_profile_my_matches = (TextView) view.findViewById(R.id.txt_profile_my_matches);
        txt_profile_my_matches.setTypeface(typeface);

        txt_profile_my_friends = (TextView) view.findViewById(R.id.txt_profile_my_friends);
        txt_profile_my_friends.setTypeface(typeface);

        txt_RangList_Profile = (TextView) view.findViewById(R.id.txt_RangList_Profile);
        txt_RangList_Profile.setTypeface(typeface);

        Typeface typeface2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");

        txt_profile_my_friendsExp = (TextView) view.findViewById(R.id.txt_profile_my_friendsExp);
        txt_profile_my_friendsExp.setTypeface(typeface2);

        txt_profile_my_matchesExp = (TextView) view.findViewById(R.id.txt_profile_my_matchesExp);
        txt_profile_my_matchesExp.setTypeface(typeface2);
        //endregion
    }
}
