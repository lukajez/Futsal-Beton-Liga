package com.example.mosis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UpdateAdapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;
    List<UserModel> userList;
    FirebaseFirestore db;

    public UpdateAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_layout, parent, false);

        db = FirebaseFirestore.getInstance();

        ViewHolder viewHolder = new ViewHolder(itemView);

        viewHolder.setOnClickLIstener(new ViewHolder.ClickListener() {

            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.modelUsername.setText(userList.get(position).getUsername());
        holder.modelTeam.setText(userList.get(position).getTeam());
        holder.modelPoins.setText(userList.get(position).getPoints());

        Uri uri;
        if(userList.get(position).getImage_url().length() > 0) {
            uri = Uri.parse(userList.get(position).getImage_url());
            Glide.with(context).load(uri).into(holder.modelImage);
        } else {
            holder.modelImage.setImageResource(R.drawable.ic_profile_user);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
