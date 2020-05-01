package com.example.mosis;

import android.graphics.Typeface;
import android.media.Image;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView modelUsername, modelTeam, modelPoins;
    CircleImageView modelImage;
    View mView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        mView = itemView;

        itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
        });

        modelUsername = itemView.findViewById(R.id.model_username);
        modelTeam = itemView.findViewById(R.id.model_team);
        modelPoins = itemView.findViewById(R.id.model_points);
        modelImage = itemView.findViewById(R.id.model_picture);

        //region FontSetUp
        Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "" +
                "fonts/adventproregular.ttf");
        modelUsername.setTypeface(typeface);
        modelTeam.setTypeface(typeface);
        modelPoins.setTypeface(typeface);
        //endregion

    }

    private ViewHolder.ClickListener mClickListener;
    public interface ClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    public void setOnClickLIstener(ViewHolder.ClickListener clickLIstener){
        mClickListener = clickLIstener;
    }


}
