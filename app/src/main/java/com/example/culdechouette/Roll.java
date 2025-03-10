package com.example.culdechouette;

import java.util.Arrays;

public class Roll {

    private final Figure figure;
    private final int figureValue;
    private final int figureScore;

    public enum Figure {
        CHOUETTE,
        CHOUETTE_VELUTE,
        CUL_DE_CHOUETTE,
        CUL_DE_CHOUETTE_SIROTE,
        VELUTE,
        SUITE,
        SUITE_VELUTE,
        SOUFLETTE,
        NEANT
    }

    private Roll (Figure figure, int figureValue, int figureScore) {
        this.figure = figure;
        this.figureValue = figureValue;
        this.figureScore = figureScore;
    }

    public Figure figure() {
        return this.figure;
    }

    public int figureValue() {
        return this.figureValue;
    }

    public int figureScore() {
        return this.figureScore;
    }

    public String figureName() {
        switch (this.figure) {
            case CHOUETTE:
                return "Chouette de " + this.figureValue;
            case CHOUETTE_VELUTE:
                return "Chouette Velute de " + this.figureValue;
            case CUL_DE_CHOUETTE:
                return "Cul de Chouette de " + this.figureValue;
            case CUL_DE_CHOUETTE_SIROTE:
                return "Cul de Chouette de " + this.figureValue + " siroté";
            case VELUTE:
                return "Velute de " + this.figureValue;
            case SUITE:
                return "Suite de " + this.figureValue;
            case SUITE_VELUTE:
                return "Suite Velute";
            case SOUFLETTE:
                return "Souflette";
            case NEANT:
            default:
                return "Néant";
        }
    }

    public static Roll roll(int dice1, int dice2, int dice3) {
        // Trier les dés pour simplifier les comparaisons
        int[] dice = {dice1, dice2, dice3};
        Arrays.sort(dice);

        // Vérifier Cul de Chouette
        if (dice[0] == dice[1] && dice[1] == dice[2]) {
            return new Roll(Figure.CUL_DE_CHOUETTE, dice[0], 40 + dice[0] * 10);
        }

        // Vérifier Chouette Velute
        if (dice[0] == dice[1] && dice[0] + dice[1] == dice[2]) {
            return new Roll(Figure.CHOUETTE_VELUTE, dice[2], 2 * dice[2] * dice[2]);
        }

        // Vérifier Chouette
        if (dice[0] == dice[1] || dice[1] == dice[2]) {
            return new Roll(Figure.CHOUETTE, dice[1], dice[1] * dice[1]);
        }

        // Vérifier Suite
        if (dice[2] == dice[1] + 1 && dice[1] == dice[0] + 1) {
            if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
                return new Roll(Figure.SUITE_VELUTE, dice[2], 18);
            }
            return new Roll(Figure.SUITE, dice[2], -10);
        }

        // Vérifier Velute
        if (dice[0] + dice[1] == dice[2]) {
            return new Roll(Figure.VELUTE, dice[2], 2 * dice[2] * dice[2]);
        }

        // Vérifier Soufflette
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 4) {
            return new Roll(Figure.SOUFLETTE, dice[0], 0);
        }

        // Si aucune combinaison, c'est un Néant
        return new Roll(Figure.NEANT, dice[0], 0);
    }
}
