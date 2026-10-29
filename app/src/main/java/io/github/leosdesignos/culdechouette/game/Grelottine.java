package io.github.leosdesignos.culdechouette.game;

import java.io.Serializable;

/** A Grelottine challenge in progress: the challenged player throws the dice to make {@link #figure()}. */
public final class Grelottine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int challenger;
    private final int target;
    private final Figure figure;
    private final int stake;
    private final int[] bettors;

    Grelottine(int challenger, int target, Figure figure, int stake, int[] bettors) {
        this.challenger = challenger;
        this.target = target;
        this.figure = figure;
        this.stake = stake;
        this.bettors = bettors;
    }

    public int challenger() {
        return challenger;
    }

    /** The challenged player. */
    public int target() {
        return target;
    }

    public Figure figure() {
        return figure;
    }

    public int stake() {
        return stake;
    }

    /** Players betting on the success of the challenge. */
    public int[] bettors() {
        return bettors.clone();
    }
}
