package com.example.mosis;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class MatchLocation {

    private GeoPoint geoPoint;
    //server timestamp annotation, pass Null => insert timestamp
    private @ServerTimestamp
    Date timestamp;
    private MatchModel match;

    public MatchLocation() {}

    public MatchLocation(GeoPoint geoPoint, Date timestamp, MatchModel match, UserModel userModel) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.match = match;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public MatchModel getMatch() {
        return match;
    }

    public void setMatch(MatchModel match) {
        this.match = match;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserLocation{ geoPoint: " + geoPoint + ", timestamp: " + timestamp + ", user: " + match + " }";
    }
}
