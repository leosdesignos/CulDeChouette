package com.example.culdechouette;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Roll {

    private final Figure figure;
    private final String figureName;
    private final int figureValue;
    private final int figureScore;

    public enum Figure {
        // TODO replace with R.string.figure_<name>
        CHOUETTE("Chouette"),
        CHOUETTE_VELUTE("Chouette velute"),
        CUL_DE_CHOUETTE("Cul de chouette"),
        CUL_DE_CHOUETTE_SIROTE("Cul de chouette siroté"),
        VELUTE("Velute"),
        SUITE("Suite"),
        SUITE_VELUTE("Suite velute"),
        SOUFLETTE("Souflette"),
        NEANT("Néant");

        private final String id;

        Figure(String id) {
            this.id = id;
        }

        @NonNull
        @Override
        public String toString() {
            return this.id;
        }
    }

    public Roll (int dice1, int dice2, int dice3) {
        Roll roll = fromDices(dice1, dice2, dice3);
        this.figure = roll.figure;
        this.figureName = roll.figureName;
        this.figureValue = roll.figureValue;
        this.figureScore = roll.figureScore;
    }

    public Roll (Figure figure, int figureValue) {
        this.figure = figure;
        this.figureValue = figureValue;
        this.figureName = processName(figure, figureValue);
        this.figureScore = processScore(figure, figureValue);
    }

    public Figure figure() {
        return this.figure;
    }

    public String figureName() {
        return this.figureName;
    }

    public int figureValue() {
        return this.figureValue;
    }

    public int figureScore() {
        return this.figureScore;
    }

    private static Roll fromDices(int dice1, int dice2, int dice3) {
        // Trier les dés pour simplifier les comparaisons
        int[] dice = {dice1, dice2, dice3};
        Arrays.sort(dice);

        // Vérifier Cul de Chouette
        if (dice[0] == dice[1] && dice[1] == dice[2]) {
            return new Roll(Figure.CUL_DE_CHOUETTE, dice[0]);
        }

        // Vérifier Chouette Velute
        if (dice[0] == dice[1] && dice[0] + dice[1] == dice[2]) {
            return new Roll(Figure.CHOUETTE_VELUTE, dice[2]);
        }

        // Vérifier Chouette
        if (dice[0] == dice[1] || dice[1] == dice[2]) {
            return new Roll(Figure.CHOUETTE, dice[1]);
        }

        // Vérifier Suite
        if (dice[2] == dice[1] + 1 && dice[1] == dice[0] + 1) {
            if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
                return new Roll(Figure.SUITE_VELUTE, dice[2]);
            }
            return new Roll(Figure.SUITE, dice[2]);
        }

        // Vérifier Velute
        if (dice[0] + dice[1] == dice[2]) {
            return new Roll(Figure.VELUTE, dice[2]);
        }

        // Vérifier Soufflette
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 4) {
            return new Roll(Figure.SOUFLETTE, dice[0]);
        }

        // Si aucune combinaison, c'est un Néant
        return new Roll(Figure.NEANT, dice[0]);
    }

    private static String processName(Figure figure, int figureValue) {
        switch (figure) {
            case CHOUETTE:
                return "Chouette de " + figureValue;
            case CHOUETTE_VELUTE:
                return "Chouette Velute de " + figureValue;
            case CUL_DE_CHOUETTE:
                return "Cul de Chouette de " + figureValue;
            case CUL_DE_CHOUETTE_SIROTE:
                return "Cul de Chouette de " + figureValue + " siroté";
            case VELUTE:
                return "Velute de " + figureValue;
            case SUITE:
                return "Suite de " + figureValue;
            case SUITE_VELUTE:
                return "Suite Velute";
            case SOUFLETTE:
                return "Souflette";
            case NEANT:
            default:
                return "Néant";
        }
    }

    private static int processScore(Figure figure, int figureValue) {
        switch (figure) {
            case CHOUETTE:
                return figureValue * figureValue;
            case CUL_DE_CHOUETTE:
            case CUL_DE_CHOUETTE_SIROTE:
                return 40 + figureValue * 10;
            case VELUTE:
            case SUITE_VELUTE:
            case CHOUETTE_VELUTE:
                return 2 * figureValue * figureValue;
            case SUITE:
                return -10;
            case SOUFLETTE:
            case NEANT:
            default:
                return 0;
        }
    }

}
