package com.example.mosis;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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

    //SearchFragment searchFragment;
    Context context;
    List<Users> usersList;

    public CustomAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
//        this.context = context;
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

                //Users users = new Users(usersList.get(position));

                Intent intent = new Intent( context, FriendActivity.class);

                intent.putExtra("username", usersList.get(position).getUsername());
                intent.putExtra("team", usersList.get(position).getTeam());
                intent.putExtra("points", usersList.get(position).getPoints());
                intent.putExtra("image", usersList.get(position).getImage());

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

        holder.modelUsername.setText(usersList.get(position).getUsername());
        holder.modelTeam.setText(usersList.get(position).getTeam());
        holder.modelPoins.setText(usersList.get(position).getPoints());

        Uri uri;
        if(usersList.get(position).getImage().length() > 0) {
            uri = Uri.parse(usersList.get(position).getImage());
            Glide.with(context).load(uri).into(holder.modelImage);
        } else {
            holder.modelImage.setImageResource(R.drawable.ic_profile_user);
        }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }
}
