package com.example.culdechouette;

import java.io.Serializable;

public class Player implements Serializable {
    private final String playerName;

    private int score;
    private boolean grelottine;
    private boolean civet;

    Player(String playerName) {
        this.playerName = playerName;
        this.score = 0;
        this.grelottine = false;
        this.civet = false;
    }

    public String name() {
        return this.playerName;
    }

    public int score () {
        return this.score;
    }

    public void addScore (int score) {
        this.score += score;
    }

    public boolean grelottine () {
        return this.grelottine;
    }

    public void setGrelottine (boolean grelottine) {
        this.grelottine = grelottine;
    }

    public boolean civet() {
        return this.civet;
    }

    public void setCivet(boolean civet) {
        this.civet = civet;
    }
}
