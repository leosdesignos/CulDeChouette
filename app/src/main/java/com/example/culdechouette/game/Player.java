package com.example.culdechouette.game;

import java.io.Serializable;

/** A player, with the score and tokens (Grelottine, Civet, Passe-Grelot) they hold. */
public final class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    /** A player can benefit from a single Passe-Grelot per game. */
    public enum PasseGrelot { NONE, AVAILABLE, USED }

    private final String name;

    int score;
    boolean grelottine;
    boolean civet;
    PasseGrelot passeGrelot = PasseGrelot.NONE;
    boolean eliminated;

    Player(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public int score() {
        return score;
    }

    public boolean hasGrelottine() {
        return grelottine;
    }

    public boolean hasCivet() {
        return civet;
    }

    public PasseGrelot passeGrelot() {
        return passeGrelot;
    }

    /** Players reaching -343 points leave the game. */
    public boolean isEliminated() {
        return eliminated;
    }

    Player copy() {
        Player copy = new Player(name);
        copy.score = score;
        copy.grelottine = grelottine;
        copy.civet = civet;
        copy.passeGrelot = passeGrelot;
        copy.eliminated = eliminated;
        return copy;
    }

    @Override
    public String toString() {
        return name;
    }
}
