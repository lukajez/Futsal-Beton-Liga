package com.example.mosis;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class  ViewMatchHolder extends  RecyclerView.ViewHolder {

    TextView modelName, modelStatus, modelDate, modelType;
    CircleImageView modelImage;
    View mView;

    public ViewMatchHolder(@NonNull View itemView) {
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

        modelName = itemView.findViewById(R.id.model_name);
        modelStatus = itemView.findViewById(R.id.model_status);
        modelDate = itemView.findViewById(R.id.model_date);
        modelType = itemView.findViewById(R.id.model_type);
        modelImage = itemView.findViewById(R.id.model_image);

        if(modelStatus.equals("not-available")){
            modelStatus.setTextColor(Color.parseColor("#f54254"));
        } else {
            modelStatus.setTextColor(Color.parseColor("#03fc52"));
        }

        //region FontSetUp
        Typeface _typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "" +
                "fonts/bebasneue.ttf");
        modelName.setTypeface(_typeface);

        Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), "" +
                "fonts/adventproregular.ttf");
        modelStatus.setTypeface(typeface);
        modelDate.setTypeface(typeface);
        modelType.setTypeface(typeface);
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
