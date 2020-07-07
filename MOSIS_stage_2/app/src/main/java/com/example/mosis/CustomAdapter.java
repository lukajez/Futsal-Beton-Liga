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

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {

    Context context;
    List<User> userList;

    public CustomAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemView);

        viewHolder.setOnClickLIstener(new ViewHolder.ClickListener() {
            //set on click to go to
            @Override
            public void onItemClick(View view, int position) {

                //User users = new User(userList.get(position));

                Intent intent = new Intent( context, FriendActivity.class);

                intent.putExtra("username", userList.get(position).getUsername());
                intent.putExtra("team", userList.get(position).getTeam());
                intent.putExtra("points", userList.get(position).getPoints());
                intent.putExtra("image", userList.get(position).getImage());

                context.startActivity(intent);
                Activity activity = (Activity) context;
                activity.overridePendingTransition(0, 0);
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
        if(userList.get(position).getImage().length() > 0) {
            uri = Uri.parse(userList.get(position).getImage());
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
