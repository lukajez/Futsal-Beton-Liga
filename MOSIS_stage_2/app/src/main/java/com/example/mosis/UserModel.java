package com.example.mosis;

import java.util.ArrayList;

public class UserModel {

    private String user_id;
    private String email;
    private String username;
    private int points;
    private String team;
    private ArrayList<String> friends = new ArrayList<>();
    private ArrayList<MatchModel> matches = new ArrayList<>();
    private ArrayList<MatchModel> subscriptions = new ArrayList<>();
    private String image_url;

    public UserModel(){}

    public UserModel(String user_id, String email, String username, int points, String team, ArrayList<String> friends, ArrayList<MatchModel> matches, ArrayList<MatchModel> subscriptions, String image_url) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.points = points;
        this.team = team;
        this.friends = friends;
        this.matches = matches;
        this.subscriptions = subscriptions;
        this.image_url = image_url;
    }

//    public UserModel(String user_id, String email, String username, int points, String team, ArrayList<String> friends, ArrayList<MatchModel> matches, String image_url) {
//        this.user_id = user_id;
//        this.email = email;
//        this.username = username;
//        this.points = points;
//        this.team = team;
//        this.friends = friends;
//        this.matches = matches;
//        this.image_url = image_url;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public ArrayList<String> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public ArrayList<MatchModel> getMatches() {
        return matches;
    }

    public void setMatches(ArrayList<MatchModel> matches) {
        this.matches = matches;
    }

    public ArrayList<MatchModel> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<MatchModel> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
