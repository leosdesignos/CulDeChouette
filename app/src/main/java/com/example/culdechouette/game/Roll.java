package com.example.culdechouette.game;

import java.io.Serializable;
import java.util.Arrays;

/** The combination formed by a throw of the three dice. */
public final class Roll implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Figure figure;
    private final int value;

    private Roll(Figure figure, int value) {
        this.figure = figure;
        this.value = value;
    }

    public static Roll of(int dice1, int dice2, int dice3) {
        int[] dice = {dice1, dice2, dice3};
        for (int die : dice) {
            if (die < 1 || die > 6) {
                throw new IllegalArgumentException("Invalid die: " + die);
            }
        }
        // Sort the dice so the order they were thrown in doesn't matter
        Arrays.sort(dice);

        if (dice[0] == dice[2]) {
            return new Roll(Figure.CUL_DE_CHOUETTE, dice[0]);
        }
        if (dice[0] == dice[1] && 2 * dice[0] == dice[2]) {
            return new Roll(Figure.CHOUETTE_VELUTE, dice[2]);
        }
        if (dice[0] == dice[1] || dice[1] == dice[2]) {
            return new Roll(Figure.CHOUETTE, dice[1]);
        }
        if (dice[1] == dice[0] + 1 && dice[2] == dice[1] + 1) {
            return new Roll(dice[0] == 1 ? Figure.SUITE_VELUTE : Figure.SUITE, dice[2]);
        }
        if (dice[0] + dice[1] == dice[2]) {
            return new Roll(Figure.VELUTE, dice[2]);
        }
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 4) {
            return new Roll(Figure.SOUFFLETTE, 0);
        }
        return new Roll(Figure.NEANT, 0);
    }

    /** The Cul de Chouette obtained by a successful Sirotage of a Chouette of {@code value}. */
    static Roll siroted(int value) {
        return new Roll(Figure.CUL_DE_CHOUETTE_SIROTE, value);
    }

    public Figure figure() {
        return figure;
    }

    /** Value of the die that names the figure, 0 for figures without a value. */
    public int value() {
        return value;
    }

    /** Points the combination is worth. */
    public int points() {
        switch (figure) {
            case CHOUETTE:
                return value * value;
            case VELUTE:
            case CHOUETTE_VELUTE:
            case SUITE_VELUTE:
                return 2 * value * value;
            case CUL_DE_CHOUETTE:
            case CUL_DE_CHOUETTE_SIROTE:
                return 40 + 10 * value;
            default:
                return 0;
        }
    }
}
