package com.example.mosis;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MatchModel {

    private String id;
    private String name;
    private String type; //single, tournament, friendly, charity tournament...
    private UserModel creator;
    private String date;
    private ArrayList<UserModel> partitipiants;
    private UserModel winner;
    private String status; //available, not available
    private boolean played;
    private String image_url;

    public MatchModel() {

        this.name = "Default Match";
        this.partitipiants = new ArrayList<>();
        this.winner = null;
        this.status = "available";
        this.played = false;
        this.image_url = "https://firebasestorage.googleapis.com/v0/b/mosis-dc29f.appspot.com/o/default_match%2Fstreetfut.jpg?alt=media&token=c5c0b31a-21b1-492c-911a-cb5cc4b81e8a";

    }

    public MatchModel(MatchModel matchModel) {
        this.name = matchModel.getName();
        this.type = matchModel.getType();
        this.creator = matchModel.getCreator();
        this.date = matchModel.getDate();
        this.partitipiants = matchModel.getPartitipiants();
        this.winner = matchModel.getWinner();
        this.status = matchModel.getStatus();
        this.played = matchModel.isPlayed();
        this.image_url = matchModel.getImage_url();
    }

    public MatchModel(String name, String type, UserModel creator, String date, ArrayList<UserModel> participiants, UserModel winner, String status, boolean played, String image_url) {
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.date = date;
        this.partitipiants = participiants;
        this.winner = winner;
        this.status = status;
        this.played = played;
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserModel getCreator() {
        return creator;
    }

    public void setCreator(UserModel creator) {
        this.creator = creator;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<UserModel> getPartitipiants() {
        return partitipiants;
    }

    public void setPartitipiants(ArrayList<UserModel> participiants) {
        this.partitipiants = participiants;
    }

    public UserModel getWinner() {
        return winner;
    }

    public void setWinner(UserModel winnter) {
        this.winner = winnter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }




}
