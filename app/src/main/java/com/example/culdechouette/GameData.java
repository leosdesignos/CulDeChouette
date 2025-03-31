package com.example.culdechouette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameData {

    private static final int GOAL = 343;

    private static GameData instance;

    private final Grelottine grelottine;
    private final ArrayList<Player> playerList;
    private final HashMap<Player, Integer> roundScore;

    private int currentPlayerIndex;

    private GameData() {
        this.currentPlayerIndex = 0;
        this.grelottine = new Grelottine();
        this.playerList = new ArrayList<>();
        this.roundScore = new HashMap<>();
    }

    public static synchronized GameData getInstance() {
        if (instance == null) {
            instance = new GameData();
        }
        return instance;
    }

    public void endRound() {
        // Flush round points
        for (Player player : playerList) {
            if (roundScore.containsKey(player)){
                //noinspection ConstantConditions
                player.addScore(roundScore.get(player));
            }
        }
        roundScore.clear();

        // Next player
        currentPlayerIndex++;
        if (currentPlayerIndex >= playerList.size()) {
            currentPlayerIndex = 0;
        }

        grelottine.enabled = false;
    }

    public void bevue(Player player) {
        addRoundScore(player, -10);
    }

    public void addRoundScore(Player player, int score) {
        //noinspection ConstantConditions
        int playerScore = roundScore.containsKey(player) ? roundScore.get(player) : 0;
        playerScore += score;
        roundScore.put(player, playerScore);
    }

    public Player currentPlayer() {
        int index = grelottine.enabled ? grelottine.targetedIndex : currentPlayerIndex;
        return playerList.get(index);
    }

    public ArrayList<Player> playerList() {
        return playerList;
    }

    public ArrayList<Player> rankedPlayerList() {
        ArrayList<Player> rankedList = new ArrayList<>(playerList);
        Collections.sort(rankedList, (p1, p2) -> Integer.compare(p2.score(), p1.score()));
        return rankedList;
    }

    public boolean isWinner() {
        return rankedPlayerList().get(0).score() >= GOAL;
    }

    public Grelottine grelottine() {
        return grelottine;
    }

//    public void log(Player player, Roll roll) {
//        // TODO log game events
//    }


    public class Grelottine {
        public boolean enabled = false;
        private int targetedIndex;

        public Player targeted;
        public Player targeting;

        public int stake;
        public Roll.Figure figure;
        public Map<Player, Boolean> bets;

        public void start(Player targeted, Player targeting,
                          Map<Player, Boolean> bets, Roll.Figure figure, int stake) {
            this.enabled = true;
            this.targetedIndex = playerList.indexOf(targeted);
            this.targeted = targeted;
            this.targeting = targeting;
            this.figure = figure;
            this.stake = stake;
            this.bets = bets;
        }
    }

}

