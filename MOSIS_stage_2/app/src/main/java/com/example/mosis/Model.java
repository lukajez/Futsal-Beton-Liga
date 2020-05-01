package com.example.mosis;

public class Model {
    String id, mail, username, team, points, matches, friends;

    public Model() {
    }

    public Model(String id, String username, String team, String points, String matches, String friends) {
        this.id = id;
        this.username = username;
        this.team = team;
        this.points = points;
        this.matches = matches;
        this.friends = friends;
    }

    public Model(String username, String team, String points) {
        this.username = username;
        this.team = team;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getMatches() {
        return matches;
    }

    public void setMatches(String matches) {
        this.matches = matches;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }
}
