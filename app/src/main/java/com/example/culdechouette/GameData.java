package com.example.culdechouette;

import java.util.ArrayList;
import java.util.HashMap;

public class GameData {

    private static GameData instance;

    private final ArrayList<Player> playerList;
    private final HashMap<Player, Integer> roundScore;

    private int currentPlayerIndex;

    private GameData() {
        this.currentPlayerIndex = 0;
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
    }

    public Player currentPlayer() {
        return playerList.get(currentPlayerIndex);
    }

    public ArrayList<Player> playerList() {
        return playerList;
    }

    public HashMap<Player, Integer> roundScore() {
        return roundScore;
    }

//    public void log(Player player, Roll roll) {
//        // TODO log game events
//    }

}

