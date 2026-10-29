package com.example.culdechouette.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GameTest {

    private static final int A = 0;
    private static final int B = 1;
    private static final int C = 2;
    private static final int NO_BETS = 0;

    private final Game game = new Game(Arrays.asList("Arthur", "Bohort", "Calogrenant"));

    @Test
    public void chouetteScoresItsSquareAndTurnPasses() {
        game.roll(3, 3, 4);

        assertEquals(9, score(A));
        assertTrue(game.canSirote());
        game.endTurn();
        assertEquals(B, game.turn().owner());
        assertEquals(2, game.turn().number());
        assertEquals(1, game.history().size());
    }

    @Test
    public void actionsOutOfTurnAreRejectedWithoutSideEffects() {
        assertThrows(IllegalStateException.class, game::endTurn);
        assertThrows(IllegalStateException.class, () -> game.loseSuite(B, 1));
        game.roll(3, 3, 4);
        assertThrows(IllegalStateException.class, () -> game.roll(1, 2, 3));
        assertThrows(IllegalArgumentException.class, () -> game.sirote(7, bets(), Game.NOBODY));
        assertEquals(9, score(A));
    }

    @Test
    public void successfulSirotageReplacesTheChouetteAndPaysBettors() {
        setScore(B, 50);
        setScore(C, 50);
        game.roll(3, 3, 4);

        game.sirote(3, bets(NO_BETS, 3, 5), Game.NOBODY);

        assertEquals(70, score(A));
        assertEquals(Figure.CUL_DE_CHOUETTE_SIROTE, game.turn().roll().figure());
        assertEquals(70, score(B));
        assertEquals(45, score(C));
        assertFalse(game.canSirote());
    }

    @Test
    public void failedSirotageOnSixGivesCivetAndContreSirop() {
        game.roll(6, 6, 1);

        game.sirote(4, bets(), C);

        assertEquals(-36, score(A));
        assertTrue(game.player(A).hasCivet());
        assertEquals(20, score(C));
        assertEquals(-36, game.turn().pointsOf(A));
    }

    @Test
    public void playersWithoutPointsCannotBet() {
        game.roll(3, 3, 4);

        assertThrows(IllegalArgumentException.class, () -> game.sirote(3, bets(NO_BETS, 3, 0), Game.NOBODY));
        assertTrue(game.canSirote());
    }

    @Test
    public void chouetteVeluteGoesToWhoeverClaimsIt() {
        game.roll(2, 2, 4);

        assertEquals(0, score(A));
        assertFalse(game.canEndTurn());
        game.claimChouetteVelute(B);
        assertEquals(32, score(B));
        assertTrue(game.canEndTurn());
    }

    @Test
    public void suiteVeluteScoresTheVeluteAndSomeoneLosesTheSuite() {
        game.roll(1, 2, 3);

        assertEquals(18, score(A));
        assertFalse(game.canEndTurn());
        game.loseSuite(C, 2);
        assertEquals(-20, score(C));
        assertTrue(game.canEndTurn());
    }

    @Test
    public void souffletteChallenge() {
        game.roll(4, 2, 1);
        game.soufflette(B, 1);
        assertEquals(50, score(B));
        assertEquals(-50, score(A));
        assertFalse(game.canSoufflette());

        Game other = new Game(Arrays.asList("Arthur", "Bohort"));
        other.roll(4, 2, 1);
        other.soufflette(B, 0);
        assertEquals(-30, other.player(B).score());
        assertEquals(30, other.player(A).score());
    }

    @Test
    public void civetWithoutCivetIsABevueRightAway() {
        setScore(A, 50);

        assertEquals(Game.Outcome.BEVUE, game.declareCivet(Figure.CHOUETTE, 20));
        assertEquals(40, score(A));
        assertNull(game.turn().civetTarget());

        // Winning a Civet during the turn doesn't make the announcement valid afterwards
        game.roll(6, 6, 1);
        game.sirote(2, bets(), Game.NOBODY);
        assertTrue(game.player(A).hasCivet());
        game.endTurn();
        assertEquals(40 + 36 - 72, score(A));
    }

    @Test
    public void civetOnSuiteIsWonWithOneTwoThree() {
        setScore(A, 100);
        giveCivet(A);

        assertEquals(Game.Outcome.OK, game.declareCivet(Figure.SUITE, 50));
        assertFalse(game.player(A).hasCivet());
        game.roll(1, 2, 3);
        game.loseSuite(B, 1);
        game.endTurn();

        assertEquals(100 + 18 + 50, score(A));
    }

    @Test
    public void lostCivetCostsTheStake() {
        setScore(A, 100);
        giveCivet(A);
        game.declareCivet(Figure.CUL_DE_CHOUETTE, 30);
        game.roll(3, 3, 4);
        game.endTurn();

        assertEquals(100 + 9 - 30, score(A));
    }

    @Test
    public void civetStakeIsLimitedByScoreAndRules() {
        setScore(A, 40);
        giveCivet(A);
        assertEquals(40, game.maxCivetStake());
        assertThrows(IllegalArgumentException.class, () -> game.declareCivet(Figure.CHOUETTE, 41));

        setScore(A, 0);
        assertFalse(game.canDeclareCivet());
    }

    @Test
    public void pointsWonByABetOrChallengeAreCappedAt332() {
        setScore(A, 320);
        giveCivet(A);
        game.declareCivet(Figure.CHOUETTE, 50);
        game.roll(1, 1, 3);
        game.endTurn();

        assertEquals(332, score(A));
        assertFalse(game.isOver());
    }

    @Test
    public void winningBetIsCappedAt332() {
        setScore(B, 330);
        game.roll(3, 3, 4);
        game.sirote(3, bets(NO_BETS, 3, NO_BETS), Game.NOBODY);

        assertEquals(332, score(B));
    }

    @Test
    public void onlyTheOwnerOfTheTurnCanReach343() {
        setScore(B, 330);
        game.roll(3, 3, 6);
        game.claimChouetteVelute(B);
        assertEquals(342, score(B));
        game.endTurn();
        assertFalse(game.isOver());

        game.roll(1, 1, 3);
        game.endTurn();
        assertEquals(343, score(B));
        assertEquals(B, game.winner());
        assertFalse(game.canRoll());
    }

    @Test
    public void grelottineChallengeSucceeds() {
        setScore(A, 100);
        setScore(B, 200);
        setScore(C, 50);
        giveGrelottine(B);
        game.roll(1, 3, 6);
        assertTrue(game.player(A).hasGrelottine());
        assertTrue(game.canGrelottine());

        assertEquals(Game.Outcome.OK, game.announceGrelottine(B));
        assertEquals(33, game.maxGrelottineStake(B, A, Figure.CHOUETTE));
        game.startGrelottine(A, Figure.CHOUETTE, 33, new int[]{C});
        assertFalse(game.player(A).hasGrelottine());
        assertFalse(game.player(B).hasGrelottine());
        assertEquals(A, game.roller());
        assertTrue(game.canRoll());

        game.roll(5, 5, 2);
        game.endTurn();

        assertEquals(100 + 25 + 33, score(A));
        assertEquals(200 - 33, score(B));
        assertEquals(50 + 20, score(C));
        assertEquals(Player.PasseGrelot.AVAILABLE, game.player(A).passeGrelot());
    }

    @Test
    public void failedGrelottineGivesNoPasseGrelot() {
        setScore(A, 100);
        setScore(B, 100);
        giveGrelottine(B);
        game.roll(1, 3, 6);
        game.announceGrelottine(B);
        game.startGrelottine(A, Figure.CUL_DE_CHOUETTE, 16, new int[0]);
        game.roll(1, 3, 6);
        assertFalse("No Grelottine during a Grelottine", game.canGrelottine());
        game.endTurn();

        assertEquals(84, score(A));
        assertEquals(116, score(B));
        assertEquals("Only the challenged player who succeeds gets one",
                Player.PasseGrelot.NONE, game.player(B).passeGrelot());
        assertEquals(Player.PasseGrelot.NONE, game.player(A).passeGrelot());
    }

    @Test
    public void challengerWithoutGrelottineMakesABevue() {
        setScore(B, 50);
        game.roll(1, 3, 6);

        assertEquals(Game.Outcome.BEVUE, game.announceGrelottine(B));
        assertEquals(40, score(B));
        assertThrows(IllegalStateException.class,
                () -> game.startGrelottine(A, Figure.CHOUETTE, 0, new int[0]));
    }

    @Test
    public void grelottineNeedsEveryScorePositive() {
        setScore(C, -5);
        giveGrelottine(B);
        game.roll(1, 3, 6);

        assertFalse(game.canGrelottine());
        assertTrue(game.canEndTurn());
    }

    @Test
    public void passeGrelotRecomputesTheMaximumStake() {
        setScore(A, 200);
        setScore(B, 300);
        setScore(C, 30);
        game.player(A).passeGrelot = Player.PasseGrelot.AVAILABLE;
        giveGrelottine(B);
        game.roll(1, 3, 6);

        game.announceGrelottine(B);
        assertEquals(Game.Outcome.OK, game.passeGrelot());
        assertEquals(Player.PasseGrelot.USED, game.player(A).passeGrelot());
        assertThrows(IllegalArgumentException.class,
                () -> game.startGrelottine(A, Figure.CHOUETTE, 9, new int[0]));
        assertThrows(IllegalArgumentException.class,
                () -> game.startGrelottine(C, Figure.CHOUETTE, 66, new int[0]));
        assertEquals(9, game.maxGrelottineStake(B, C, Figure.CHOUETTE));
        game.startGrelottine(C, Figure.CHOUETTE, 9, new int[0]);

        assertEquals(C, game.roller());
        game.roll(2, 3, 6);
        game.endTurn();
        assertEquals(309, score(B));
        assertEquals(21, score(C));
        assertTrue("Not involved in the challenge", game.player(A).hasGrelottine());
    }

    @Test
    public void passeGrelotCancelsTheChallengeWithTwoPlayers() {
        Game duel = new Game(Arrays.asList("Arthur", "Bohort"));
        duel.player(A).passeGrelot = Player.PasseGrelot.AVAILABLE;
        duel.player(B).grelottine = true;
        duel.roll(1, 3, 6);
        duel.announceGrelottine(B);

        assertEquals(Game.Outcome.CANCELLED, duel.passeGrelot());
        assertFalse(duel.player(A).hasGrelottine());
        assertFalse(duel.player(B).hasGrelottine());
        assertFalse(duel.canGrelottine());
        assertTrue(duel.canEndTurn());
    }

    @Test
    public void passeGrelotWithoutImmunityIsABevueButTheChallengeGoesOn() {
        giveGrelottine(B);
        game.roll(1, 3, 6);
        game.announceGrelottine(B);

        assertEquals(Game.Outcome.BEVUE, game.passeGrelot());
        assertEquals(-10, score(A));
        assertFalse("A single Bévue per false claim", game.canPasseGrelot());
        assertThrows(IllegalStateException.class, game::passeGrelot);
        assertTrue(game.canGrelottine());
        game.startGrelottine(A, Figure.CHOUETTE, 0, new int[0]);
        assertEquals(A, game.roller());
    }

    @Test
    public void announcedGrelottineMustBeTakenUp() {
        giveGrelottine(B);
        game.roll(1, 3, 6);
        assertTrue(game.canEndTurn());

        game.announceGrelottine(B);

        assertFalse(game.canEndTurn());
        game.startGrelottine(A, Figure.CHOUETTE, 0, new int[0]);
        game.roll(3, 3, 4);
        assertTrue(game.canEndTurn());
    }

    @Test
    public void playersReachingMinus343LeaveTheGame() {
        setScore(B, -340);
        game.roll(4, 5, 6);
        game.loseSuite(B, 1);
        game.endTurn();

        assertTrue(game.player(B).isEliminated());
        assertEquals(C, game.turn().owner());
        assertEquals(Arrays.asList(A, C), game.otherPlayers());
    }

    @Test
    public void lastPlayerStandingWins() {
        Game duel = new Game(Arrays.asList("Arthur", "Bohort"));
        duel.player(B).score = -340;
        duel.roll(4, 5, 6);
        duel.loseSuite(B, 1);
        duel.endTurn();

        assertEquals(A, duel.winner());
    }

    @Test
    public void historyKeepsTheTokensOfEveryTurn() {
        game.roll(1, 3, 6);
        game.endTurn();

        TurnRecord record = game.history().get(0);
        assertEquals(1, record.number());
        assertEquals(A, record.owner());
        assertEquals(GameEvent.Type.ROLL, record.events().get(0).type());
        assertEquals(GameEvent.Type.GRELOTTINE_GAINED, record.events().get(1).type());
        assertTrue(record.standings().get(A).hasGrelottine());
    }

    @Test
    public void gameSurvivesSerialization() {
        game.roll(3, 3, 4);
        game.endTurn();
        game.roll(2, 2, 4);

        Game copy = GameSerializer.fromBytes(GameSerializer.toBytes(game));

        assertEquals(9, copy.player(A).score());
        assertEquals(B, copy.turn().owner());
        assertTrue(copy.turn().isChouetteVelutePending());
        List<TurnRecord> history = copy.history();
        assertEquals(1, history.size());
    }

    private int[] bets(int... values) {
        int[] bets = new int[game.players().size()];
        System.arraycopy(values, 0, bets, 0, values.length);
        return bets;
    }

    private int score(int player) {
        return game.player(player).score();
    }

    private void setScore(int player, int score) {
        game.player(player).score = score;
    }

    private void giveCivet(int player) {
        game.player(player).civet = true;
    }

    private void giveGrelottine(int player) {
        game.player(player).grelottine = true;
    }
}
