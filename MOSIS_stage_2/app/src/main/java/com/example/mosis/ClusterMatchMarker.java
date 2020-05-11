package com.example.mosis;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMatchMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private Uri imageUrl;
    private MatchModel match;

    public ClusterMatchMarker() {
    }

    public ClusterMatchMarker(LatLng position, String title, String snippet, Uri imageUrl, MatchModel match) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.imageUrl = imageUrl;
        this.match = match;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    public Uri getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MatchModel getMatch() {
        return match;
    }

    public void setMatch(MatchModel match) {
        this.match = match;
    }
}
