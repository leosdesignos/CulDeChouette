package com.example.culdechouette;

import androidx.annotation.NonNull;

public class Player {
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

    public boolean setGrelottine (boolean grelottine) {
        boolean toggled = this.grelottine != grelottine;
        this.grelottine = grelottine;
        return toggled;
    }

    public boolean civet() {
        return this.civet;
    }

    public boolean setCivet(boolean civet) {
        boolean toggled = this.civet != civet;
        this.civet = civet;
        return toggled;
    }

    @NonNull
    @Override
    public String toString() {
        return name();
    }
}
