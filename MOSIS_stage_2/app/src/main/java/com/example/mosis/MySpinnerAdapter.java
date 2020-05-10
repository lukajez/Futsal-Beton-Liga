package com.example.mosis;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MySpinnerAdapter extends ArrayAdapter<String> {

    Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");


    public MySpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    public MySpinnerAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        TextView view = (TextView) super.getView(position, convertView, parent);
        view.setTypeface(typeface);

        return view;
    }


//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        TextView view = (TextView) super.getView(position, convertView, parent);
//        view.setTypeface(font);
//        return view;
//    }

    // Affects opened state of the spinner
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        view.setTypeface(typeface);
        return view;
    }

}
