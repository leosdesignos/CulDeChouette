package io.github.leosdesignos.culdechouette.game;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Something that happened during a turn. Points are the ones actually applied to the scores,
 * after the caps of the end of game rules.
 */
public final class GameEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        /** {@link #player()} threw {@link #figure()} of {@link #value()}. */
        ROLL,
        /** {@link #value()} is the value of the Chouette. */
        SIROTAGE_WON,
        SIROTAGE_LOST,
        /** {@link #value()} is the die the player bet on. */
        SIROTAGE_BET_WON,
        SIROTAGE_BET_LOST,
        CONTRE_SIROP,
        CHOUETTE_VELUTE_CLAIMED,
        CHOUETTE_VELUTE_UNCLAIMED,
        SUITE_LOST,
        /** {@link #player()} is the challenged player, {@link #other()} the challenger, {@link #value()} the throws needed. */
        SOUFFLETTE_WON,
        SOUFFLETTE_LOST,
        /** {@link #value()} is the stake. */
        CIVET_ANNOUNCED,
        CIVET_WON,
        CIVET_LOST,
        GRELOTTINE_GAINED,
        CIVET_GAINED,
        PASSE_GRELOT_GAINED,
        /** {@link #player()} is the challenger, {@link #other()} the challenged player, {@link #value()} the stake. */
        GRELOTTINE_CHALLENGE,
        /** {@link #other()} is the player the challenge is passed to. */
        PASSE_GRELOT_USED,
        /** {@link #other()} is the challenger. */
        GRELOTTINE_CANCELLED,
        /** {@link #player()} is the challenged player, {@link #other()} the challenger. */
        GRELOTTINE_WON,
        GRELOTTINE_LOST,
        GRELOTTINE_BET_WON,
        GRELOTTINE_BET_LOST,
        BEVUE_NO_CIVET,
        BEVUE_NO_GRELOTTINE,
        BEVUE_NO_PASSE_GRELOT,
        ELIMINATED,
        WINNER
    }

    private final Type type;
    private final int player;
    private final int points;
    private final int other;
    private final int otherPoints;
    @Nullable
    private final Figure figure;
    private final int value;

    GameEvent(Type type, int player, int points, int other, int otherPoints,
              @Nullable Figure figure, int value) {
        this.type = type;
        this.player = player;
        this.points = points;
        this.other = other;
        this.otherPoints = otherPoints;
        this.figure = figure;
        this.value = value;
    }

    public Type type() {
        return type;
    }

    /** Index of the player concerned, -1 for nobody. */
    public int player() {
        return player;
    }

    /** Points won (or lost when negative) by {@link #player()}. */
    public int points() {
        return points;
    }

    /** Index of a second player concerned, -1 for nobody. */
    public int other() {
        return other;
    }

    /** Points won (or lost when negative) by {@link #other()}. */
    public int otherPoints() {
        return otherPoints;
    }

    @Nullable
    public Figure figure() {
        return figure;
    }

    public int value() {
        return value;
    }
}
