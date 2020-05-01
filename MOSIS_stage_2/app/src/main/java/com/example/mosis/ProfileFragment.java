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

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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


public class ProfileFragment extends Fragment {

    TextView txtUsername;
    TextView txtTeam;
    TextView txtPoints;
    View view;
    FirebaseFirestore db;
    FirebaseUser user;
    ImageView imageView;
    Uri pickedImage;
    private static final int TAKE_IMAGE_CODE = 10001;

    public ProfileFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

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

        //endregion
    }
}
