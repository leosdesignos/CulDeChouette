package io.github.leosdesignos.culdechouette.game;

/** Combinations formed by the three dice. */
public enum Figure {
    CHOUETTE(33),
    VELUTE(25),
    CHOUETTE_VELUTE(8),
    SUITE(14),
    SUITE_VELUTE(0),
    CUL_DE_CHOUETTE(16),
    CUL_DE_CHOUETTE_SIROTE(20),
    SOUFFLETTE(0),
    NEANT(0);

    private static final Figure[] BETTABLE = {
            CHOUETTE, VELUTE, CHOUETTE_VELUTE, SUITE, CUL_DE_CHOUETTE, CUL_DE_CHOUETTE_SIROTE
    };

    private final int grelottinePercent;

    Figure(int grelottinePercent) {
        this.grelottinePercent = grelottinePercent;
    }

    /** Figures a Civet or a Grelottine challenge can be placed on, in display order. */
    public static Figure[] bettable() {
        return BETTABLE.clone();
    }

    /** Percentage of the lowest score that can be put at stake in a Grelottine challenge on this figure. */
    public int grelottinePercent() {
        return grelottinePercent;
    }

    /** Whether a coup of this figure fulfils a Civet or Grelottine target: 1-2-3 is both a Suite and a Velute. */
    public boolean fulfils(Figure target) {
        return this == target || (this == SUITE_VELUTE && (target == SUITE || target == VELUTE));
    }
}
