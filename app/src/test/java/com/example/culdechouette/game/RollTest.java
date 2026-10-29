package com.example.culdechouette.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.EnumMap;
import java.util.Map;

public class RollTest {

    @Test
    public void everyThrowHasTheExpectedFigure() {
        Map<Figure, Integer> counts = new EnumMap<>(Figure.class);
        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                for (int d3 = 1; d3 <= 6; d3++) {
                    Figure figure = Roll.of(d1, d2, d3).figure();
                    counts.put(figure, counts.getOrDefault(figure, 0) + 1);
                }
            }
        }
        assertEquals(6, (int) counts.get(Figure.CUL_DE_CHOUETTE));
        assertEquals(9, (int) counts.get(Figure.CHOUETTE_VELUTE));
        assertEquals(81, (int) counts.get(Figure.CHOUETTE));
        assertEquals(6, (int) counts.get(Figure.SUITE_VELUTE));
        assertEquals(18, (int) counts.get(Figure.SUITE));
        assertEquals(30, (int) counts.get(Figure.VELUTE));
        assertEquals(6, (int) counts.get(Figure.SOUFFLETTE));
        assertEquals(60, (int) counts.get(Figure.NEANT));
    }

    @Test
    public void figuresAndPointsFollowTheRules() {
        assertRoll(Roll.of(3, 4, 3), Figure.CHOUETTE, 3, 9);
        assertRoll(Roll.of(6, 2, 4), Figure.VELUTE, 6, 72);
        assertRoll(Roll.of(4, 2, 2), Figure.CHOUETTE_VELUTE, 4, 32);
        assertRoll(Roll.of(6, 6, 6), Figure.CUL_DE_CHOUETTE, 6, 100);
        assertRoll(Roll.of(1, 1, 1), Figure.CUL_DE_CHOUETTE, 1, 50);
        assertRoll(Roll.of(3, 1, 2), Figure.SUITE_VELUTE, 3, 18);
        assertRoll(Roll.of(5, 4, 6), Figure.SUITE, 6, 0);
        assertRoll(Roll.of(4, 2, 1), Figure.SOUFFLETTE, 0, 0);
        assertRoll(Roll.of(1, 3, 6), Figure.NEANT, 0, 0);
        assertEquals(70, Roll.siroted(3).points());
    }

    @Test
    public void diceMustBeBetweenOneAndSix() {
        assertThrows(IllegalArgumentException.class, () -> Roll.of(0, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> Roll.of(1, 7, 3));
    }

    @Test
    public void suiteVeluteFulfilsSuiteAndVelute() {
        assertTrue(Figure.SUITE_VELUTE.fulfils(Figure.SUITE));
        assertTrue(Figure.SUITE_VELUTE.fulfils(Figure.VELUTE));
        assertFalse(Figure.SUITE.fulfils(Figure.VELUTE));
        assertFalse(Figure.CHOUETTE_VELUTE.fulfils(Figure.CHOUETTE));
        assertFalse(Figure.CUL_DE_CHOUETTE.fulfils(Figure.CUL_DE_CHOUETTE_SIROTE));
    }

    private static void assertRoll(Roll roll, Figure figure, int value, int points) {
        assertEquals(figure, roll.figure());
        assertEquals(value, roll.value());
        assertEquals(points, roll.points());
    }
}
