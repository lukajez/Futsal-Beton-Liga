package com.example.mosis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CustomMatchAdapter extends RecyclerView.Adapter<ViewMatchHolder> {

    Context context;
    ArrayList<MatchModel> matchList;

    public CustomMatchAdapter(Context context, ArrayList<MatchModel> matchList) {

        this.context = context;
        this.matchList = matchList;

        Log.d("29 TAG", "CustomMatchAdapter: " + this.matchList);
    }

    @NonNull
    @Override
    public ViewMatchHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_model_layout, parent, false);

        ViewMatchHolder viewHolder = new ViewMatchHolder(itemView);

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
    public void onBindViewHolder(@NonNull ViewMatchHolder holder, int position) {

        holder.modelName.setText(matchList.get(position).getName());

        Log.d("61 TAG", "CustomMatchAdapter: " + this.matchList.get(position).getName());

        holder.modelStatus.setText(matchList.get(position).getStatus());
        holder.modelDate.setText(matchList.get(position).getDate());
        holder.modelType.setText(matchList.get(position).getType());

        Uri uri;
        if(matchList.get(position).getImage_url().length() > 0) {
            uri = Uri.parse(matchList.get(position).getImage_url());
            Glide.with(context).load(uri).into(holder.modelImage);
        } else {
            holder.modelImage.setImageResource(R.drawable.ic_profile_user);
        }
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }
}
