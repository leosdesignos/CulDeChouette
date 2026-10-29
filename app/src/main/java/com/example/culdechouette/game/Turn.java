package com.example.culdechouette.game;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** State of the turn in progress. Only {@link Game} changes it. */
public final class Turn implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int number;
    private final int owner;

    final List<GameEvent> events = new ArrayList<>();

    /** Roll of the player throwing the dice now, null until they throw. */
    @Nullable
    Roll roll;
    /** Final coup of the owner of the turn, the one a Civet is settled on. */
    @Nullable
    Roll ownerRoll;
    /** Points actually credited for {@link #roll}, reverted when the Chouette is siroted. */
    int rollPoints;
    boolean sirotageDone;
    boolean souffletteDone;
    boolean chouetteVelutePending;
    boolean suitePending;

    @Nullable
    Figure civetTarget;
    int civetStake;

    /** Player who announced a Grelottine challenge, -1 if nobody did. */
    int grelottineChallenger = -1;
    @Nullable
    Grelottine grelottine;
    boolean grelottineCancelled;
    /** Player who passed the Grelottine challenge to someone else, -1 if nobody did. */
    int passeGrelotBy = -1;

    Turn(int number, int owner) {
        this.number = number;
        this.owner = owner;
    }

    public int number() {
        return number;
    }

    /** The player whose turn it is. */
    public int owner() {
        return owner;
    }

    @Nullable
    public Roll roll() {
        return roll;
    }

    @Nullable
    public Figure civetTarget() {
        return civetTarget;
    }

    public int civetStake() {
        return civetStake;
    }

    /** The challenge in progress, once started. */
    @Nullable
    public Grelottine grelottine() {
        return grelottine;
    }

    public int grelottineChallenger() {
        return grelottineChallenger;
    }

    public int passeGrelotBy() {
        return passeGrelotBy;
    }

    /** Whether a Grelottine challenge was cancelled by a Passe-Grelot with only two players. */
    public boolean isGrelottineCancelled() {
        return grelottineCancelled;
    }

    /** Whether someone still has to be designated as the winner of the Chouette Velute. */
    public boolean isChouetteVelutePending() {
        return chouetteVelutePending;
    }

    /** Whether the loser of the Suite still has to be designated. */
    public boolean isSuitePending() {
        return suitePending;
    }

    public List<GameEvent> events() {
        return Collections.unmodifiableList(events);
    }

    /** Points won or lost by {@code player} since the beginning of the turn. */
    public int pointsOf(int player) {
        int points = 0;
        for (GameEvent event : events) {
            if (event.player() == player) {
                points += event.points();
            }
            if (event.other() == player) {
                points += event.otherPoints();
            }
        }
        return points;
    }
}
