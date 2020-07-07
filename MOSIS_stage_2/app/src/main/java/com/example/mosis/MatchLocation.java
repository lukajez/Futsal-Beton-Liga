package com.example.mosis;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MatchLocation {

    private String location_name;
    private String match_type;
    private GeoPoint geoPoint;
    private String match_status;
    //server timestamp annotation, pass Null => insert timestamp
    private @ServerTimestamp
    Date timestamp;
    private MatchModel match;

    public MatchLocation() {}

    public MatchLocation(GeoPoint geoPoint, Date timestamp, MatchModel match) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.match = match;
    }

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
        return "MatchLocation { geoPoint: " + geoPoint + ", timestamp: " + timestamp + ", match: " + match.getName() + " }";
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public String getMatch_type() {
        return match_type;
    }

    public void setMatch_type(String match_type) {
        this.match_type = match_type;
    }

    public String getMatch_status() {
        return match_status;
    }

    public void setMatch_status(String match_status) {
        this.match_status = match_status;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MatchLocation)){
            return false;
        }
        MatchLocation other = (MatchLocation) o;
        return Objects.equals(other.getMatch().getName(), this.getMatch().getName());
    }
}
