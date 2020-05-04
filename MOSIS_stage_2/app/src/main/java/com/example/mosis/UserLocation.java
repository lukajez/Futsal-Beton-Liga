package com.example.mosis;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation {

    private GeoPoint geoPoint;
    //server timestamp annotation, pass Null => insert timestamp
    private @ServerTimestamp Date timestamp;
    private UserModel user;

    public UserLocation() {}

    public UserLocation(GeoPoint geoPoint, Date timestamp, UserModel user) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.user = user;
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

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    @NonNull
    @Override
    public String toString() {
        return "UserLocation{ geoPoint: " + geoPoint + ", timestamp: " + timestamp + ", user: " + user + " }";
    }
}
