package com.example.culdechouette.game;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/** A finished turn: what happened and the state of every player at its end. */
public final class TurnRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int number;
    private final int owner;
    private final List<GameEvent> events;
    private final List<Player> standings;

    TurnRecord(int number, int owner, List<GameEvent> events, List<Player> standings) {
        this.number = number;
        this.owner = owner;
        this.events = events;
        this.standings = standings;
    }

    public int number() {
        return number;
    }

    public int owner() {
        return owner;
    }

    public List<GameEvent> events() {
        return Collections.unmodifiableList(events);
    }

    /** Copies of the players at the end of the turn, in playing order. */
    public List<Player> standings() {
        return Collections.unmodifiableList(standings);
    }
}
