package com.example.mosis;

import android.media.Image;
import android.widget.ImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users {

    public String username, team, points, image;

    public Users() {}

    public Users(Users users) {
        this.username = users.getUsername();
        this.team = users.getTeam();
        this.points = users.getPoints();
        this.image = users.getImage();
    }

    public Users(String username, String team, String points, String image) {
        this.username = username;
        this.team = team;
        this.image = image;
        this.points = points;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}
