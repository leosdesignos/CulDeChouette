package com.example.culdechouette.game;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A game of Cul de Chouette and its rules. Players are referred to by their index in
 * {@link #players()}, which is also the playing order.
 *
 * <p>Actions throw {@link IllegalStateException} when they are not allowed at this point of the
 * turn and {@link IllegalArgumentException} for invalid parameters, leaving the game unchanged.
 */
public final class Game implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int GOAL = 343;
    /** Points won through a challenge or a bet can't take a score above this. */
    public static final int CHALLENGE_CAP = 332;
    /** Players reaching this score leave the game. */
    public static final int ELIMINATION = -343;
    public static final int BEVUE_PENALTY = 10;
    public static final int CIVET_MAX_STAKE = 102;
    public static final int SUITE_PENALTY = 10;
    /** A bettor pays this when betting... */
    public static final int BET_COST = 5;
    /** ...and receives this if they win, so a winning bet is worth 20 points. */
    public static final int BET_PAYOUT = 25;
    public static final int SOUFFLETTE_FAILURE = 30;

    public static final int NOBODY = -1;

    /** Outcome of the actions a player can get wrong. */
    public enum Outcome {
        OK,
        /** The player claimed a token they don't have and lost {@link #BEVUE_PENALTY} points. */
        BEVUE,
        /** The Grelottine challenge is cancelled: with two players, it can't be passed on. */
        CANCELLED
    }

    private final List<Player> players = new ArrayList<>();
    private final List<TurnRecord> history = new ArrayList<>();
    private Turn turn;
    private int winner = NOBODY;

    public Game(List<String> names) {
        require(names.size() >= 2, "At least two players are needed");
        for (String name : names) {
            players.add(new Player(name));
        }
        turn = new Turn(1, 0);
    }

    // ---------------------------------------------------------------- State

    public List<Player> players() {
        return Collections.unmodifiableList(players);
    }

    public Player player(int index) {
        return players.get(index);
    }

    public Turn turn() {
        return turn;
    }

    public List<TurnRecord> history() {
        return Collections.unmodifiableList(history);
    }

    /** Index of the winner, {@link #NOBODY} while the game goes on. */
    public int winner() {
        return winner;
    }

    public boolean isOver() {
        return winner != NOBODY;
    }

    /** The player throwing the dice now: the owner of the turn, or the target of a Grelottine challenge. */
    public int roller() {
        Grelottine grelottine = turn.grelottine;
        return grelottine != null ? grelottine.target() : turn.owner();
    }

    public boolean isActive(int player) {
        return !players.get(player).eliminated;
    }

    /** Active players other than {@code excluded}, in playing order. */
    public List<Integer> otherPlayers(int... excluded) {
        List<Integer> others = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (isActive(i) && !contains(excluded, i)) {
                others.add(i);
            }
        }
        return others;
    }

    /** Players sorted by decreasing score. */
    public List<Player> ranking() {
        List<Player> ranking = new ArrayList<>(players);
        Collections.sort(ranking, (p1, p2) -> Integer.compare(p2.score, p1.score));
        return ranking;
    }

    /** Nobody can bet with a score negative or zero. */
    public boolean canBet(int player) {
        return isActive(player) && players.get(player).score > 0;
    }

    public boolean canDeclareCivet() {
        return !isOver() && turn.grelottine == null && turn.roll == null && turn.civetTarget == null
                && canBet(turn.owner());
    }

    public int maxCivetStake() {
        return Math.max(0, Math.min(players.get(turn.owner()).score, CIVET_MAX_STAKE));
    }

    public boolean canRoll() {
        return !isOver() && turn.roll == null;
    }

    public boolean canSirote() {
        Roll roll = turn.roll;
        return !isOver() && roll != null && roll.figure() == Figure.CHOUETTE && !turn.sirotageDone;
    }

    public boolean canSoufflette() {
        Roll roll = turn.roll;
        return !isOver() && roll != null && roll.figure() == Figure.SOUFFLETTE && !turn.souffletteDone;
    }

    /** Whether someone can challenge the owner of the turn, who just made a Néant. */
    public boolean canGrelottine() {
        Roll roll = turn.roll;
        return !isOver() && turn.grelottine == null && !turn.grelottineCancelled
                && roll != null && roll.figure() == Figure.NEANT
                && otherPlayers().size() >= 2 && allScoresPositive();
    }

    public int maxGrelottineStake(int challenger, int target, Figure figure) {
        int lowest = Math.min(players.get(challenger).score, players.get(target).score);
        return Math.max(0, lowest) * figure.grelottinePercent() / 100;
    }

    public boolean canEndTurn() {
        return !isOver() && turn.roll != null && !turn.chouetteVelutePending && !turn.suitePending;
    }

    // ---------------------------------------------------------------- Actions

    /** Before throwing the dice, the owner of the turn bets {@code stake} points on the figure they'll make. */
    public Outcome declareCivet(Figure target, int stake) {
        check(canDeclareCivet(), "No Civet now");
        require(target.grelottinePercent() > 0, "Can't bet on " + target);
        int owner = turn.owner();
        if (!players.get(owner).civet) {
            bevue(owner, GameEvent.Type.BEVUE_NO_CIVET);
            return Outcome.BEVUE;
        }
        require(stake >= 1 && stake <= maxCivetStake(), "Invalid stake " + stake);
        players.get(owner).civet = false;
        turn.civetTarget = target;
        turn.civetStake = stake;
        log(GameEvent.Type.CIVET_ANNOUNCED, owner, 0, NOBODY, 0, target, stake);
        return Outcome.OK;
    }

    public void roll(int dice1, int dice2, int dice3) {
        check(canRoll(), "No roll now");
        Roll roll = Roll.of(dice1, dice2, dice3);
        int roller = roller();
        turn.roll = roll;
        turn.sirotageDone = false;
        turn.souffletteDone = false;
        turn.chouetteVelutePending = roll.figure() == Figure.CHOUETTE_VELUTE;
        turn.suitePending = roll.figure() == Figure.SUITE || roll.figure() == Figure.SUITE_VELUTE;
        if (turn.grelottine == null) {
            turn.ownerRoll = roll;
        }
        // The points of a Chouette Velute go to whoever claims it first
        turn.rollPoints = turn.chouetteVelutePending ? 0 : credit(roller, roll.points(), false);
        log(GameEvent.Type.ROLL, roller, turn.rollPoints, NOBODY, 0, roll.figure(), roll.value());

        Player player = players.get(roller);
        if (roll.figure() == Figure.NEANT && !player.grelottine) {
            player.grelottine = true;
            log(GameEvent.Type.GRELOTTINE_GAINED, roller, 0);
        }
    }

    /**
     * The roller throws one die to turn their Chouette into a Cul de Chouette.
     *
     * @param die         value of the die thrown
     * @param bets        die each player bet on (1 to 6) or 0 for no bet, indexed by player
     * @param contreSirop player who claimed the Contre-Sirop if the Sirotage failed, or {@link #NOBODY}
     */
    public void sirote(int die, int[] bets, int contreSirop) {
        check(canSirote(), "No Sirotage now");
        require(die >= 1 && die <= 6, "Invalid die " + die);
        require(bets.length == players.size(), "One bet per player expected");
        int roller = roller();
        for (int i = 0; i < bets.length; i++) {
            require(bets[i] >= 0 && bets[i] <= 6, "Invalid bet " + bets[i]);
            require(bets[i] == 0 || (i != roller && canBet(i)), players.get(i) + " can't bet");
        }
        Roll chouette = turn.roll;
        boolean success = die == chouette.value();
        require(contreSirop == NOBODY || (!success && contreSirop != roller && isActive(contreSirop)),
                "Invalid Contre-Sirop");

        // The outcome of the Sirotage replaces the points of the Chouette
        Player player = players.get(roller);
        player.score -= turn.rollPoints;
        int points = -turn.rollPoints;
        Roll siroted = Roll.siroted(chouette.value());
        if (success) {
            turn.rollPoints = credit(roller, siroted.points(), false);
            turn.roll = siroted;
            if (turn.grelottine == null) {
                turn.ownerRoll = siroted;
            }
            log(GameEvent.Type.SIROTAGE_WON, roller, points + turn.rollPoints, NOBODY, 0, null, chouette.value());
        } else {
            turn.rollPoints = debit(roller, chouette.points());
            log(GameEvent.Type.SIROTAGE_LOST, roller, points + turn.rollPoints, NOBODY, 0, null, chouette.value());
            // Civet siroté
            if (chouette.value() == 6 && !player.civet) {
                player.civet = true;
                log(GameEvent.Type.CIVET_GAINED, roller, 0);
            }
        }
        turn.sirotageDone = true;

        for (int i = 0; i < bets.length; i++) {
            if (bets[i] != 0) {
                settleBet(i, bets[i] == die,
                        GameEvent.Type.SIROTAGE_BET_WON, GameEvent.Type.SIROTAGE_BET_LOST, bets[i]);
            }
        }
        if (contreSirop != NOBODY) {
            // Twice the tenth of the Cul de Chouette attempted
            int contreSiropPoints = credit(contreSirop, siroted.points() / 5, false);
            log(GameEvent.Type.CONTRE_SIROP, contreSirop, contreSiropPoints);
        }
    }

    /** Gives the points of the Chouette Velute to the first player who claimed it, or {@link #NOBODY}. */
    public void claimChouetteVelute(int player) {
        check(!isOver() && turn.chouetteVelutePending, "No Chouette Velute to claim");
        require(player == NOBODY || isActive(player), "Invalid player");
        if (player == NOBODY) {
            log(GameEvent.Type.CHOUETTE_VELUTE_UNCLAIMED, NOBODY, 0);
        } else {
            log(GameEvent.Type.CHOUETTE_VELUTE_CLAIMED, player, credit(player, turn.roll.points(), false));
        }
        turn.chouetteVelutePending = false;
    }

    /** The last player to shout "Grelotte ça picote !" loses 10 points, times {@code multiplier} after ties. */
    public void loseSuite(int player, int multiplier) {
        check(!isOver() && turn.suitePending, "No Suite to settle");
        require(isActive(player), "Invalid player");
        require(multiplier >= 1, "Invalid multiplier " + multiplier);
        log(GameEvent.Type.SUITE_LOST, player, debit(player, SUITE_PENALTY * multiplier));
        turn.suitePending = false;
    }

    /**
     * The roller challenged {@code target} to make 4-2-1 too.
     *
     * @param throwsNeeded throws the target needed (1 to 3), 0 if they failed
     */
    public void soufflette(int target, int throwsNeeded) {
        check(canSoufflette(), "No Soufflette now");
        int challenger = roller();
        require(target != challenger && isActive(target), "Invalid target");
        require(throwsNeeded >= 0 && throwsNeeded <= 3, "Invalid number of throws " + throwsNeeded);
        if (throwsNeeded == 0) {
            int targetPoints = debit(target, SOUFFLETTE_FAILURE);
            int challengerPoints = credit(challenger, SOUFFLETTE_FAILURE, true);
            log(GameEvent.Type.SOUFFLETTE_LOST, target, targetPoints, challenger, challengerPoints, null, 0);
        } else {
            // 50 points at the first throw, 40 at the second, 30 at the third
            int stake = 60 - 10 * throwsNeeded;
            int targetPoints = credit(target, stake, true);
            int challengerPoints = debit(challenger, stake);
            log(GameEvent.Type.SOUFFLETTE_WON, target, targetPoints, challenger, challengerPoints, null, throwsNeeded);
        }
        turn.souffletteDone = true;
    }

    /** {@code challenger} shouts "Grelottine !": checks they do have a Grelottine. */
    public Outcome checkGrelottineChallenger(int challenger) {
        check(canGrelottine(), "No Grelottine now");
        require(challenger != turn.owner() && isActive(challenger), "Invalid challenger");
        if (!players.get(challenger).grelottine) {
            bevue(challenger, GameEvent.Type.BEVUE_NO_GRELOTTINE);
            return Outcome.BEVUE;
        }
        return Outcome.OK;
    }

    /** The owner of the turn passes the challenge of {@code challenger} to someone else. */
    public Outcome passeGrelot(int challenger) {
        check(canGrelottine() && turn.passeGrelotBy == NOBODY, "No Passe-Grelot now");
        require(challenger != turn.owner() && isActive(challenger) && players.get(challenger).grelottine,
                "Invalid challenger");
        int target = turn.owner();
        Player player = players.get(target);
        if (player.passeGrelot != Player.PasseGrelot.AVAILABLE) {
            bevue(target, GameEvent.Type.BEVUE_NO_PASSE_GRELOT);
            return Outcome.BEVUE;
        }
        player.passeGrelot = Player.PasseGrelot.USED;
        turn.passeGrelotBy = target;
        if (otherPlayers().size() <= 2) {
            // Nobody else to pass the challenge to
            player.grelottine = false;
            players.get(challenger).grelottine = false;
            turn.grelottineCancelled = true;
            log(GameEvent.Type.GRELOTTINE_CANCELLED, target, 0, challenger, 0, null, 0);
            return Outcome.CANCELLED;
        }
        return Outcome.OK;
    }

    /**
     * Starts the challenge: {@code target} throws the dice next, trying to make {@code figure}.
     * After a Passe-Grelot, the target is the player the challenge was passed to.
     *
     * @param bettors players betting on the success of the challenge
     */
    public void startGrelottine(int challenger, int target, Figure figure, int stake, int[] bettors) {
        check(canGrelottine(), "No Grelottine now");
        require(challenger != turn.owner() && isActive(challenger) && players.get(challenger).grelottine,
                "Invalid challenger");
        boolean passed = turn.passeGrelotBy != NOBODY;
        require(passed ? target != turn.owner() && target != challenger && isActive(target)
                : target == turn.owner(), "Invalid target");
        require(figure.grelottinePercent() > 0, "Can't challenge on " + figure);
        require(stake >= 0 && stake <= maxGrelottineStake(challenger, target, figure), "Invalid stake " + stake);
        boolean[] betting = new boolean[players.size()];
        for (int bettor : bettors) {
            require(bettor != challenger && bettor != target && canBet(bettor) && !betting[bettor],
                    "Invalid bettor " + bettor);
            betting[bettor] = true;
        }

        // The Grelottines of both players involved disappear
        players.get(challenger).grelottine = false;
        players.get(target).grelottine = false;
        if (passed) {
            log(GameEvent.Type.PASSE_GRELOT_USED, turn.passeGrelotBy, 0, target, 0, null, 0);
        }
        log(GameEvent.Type.GRELOTTINE_CHALLENGE, challenger, 0, target, 0, figure, stake);
        turn.grelottine = new Grelottine(challenger, target, figure, stake, bettors.clone());
        turn.roll = null;
        turn.rollPoints = 0;
    }

    /** Settles the Civet and the Grelottine challenge, then gives the dice to the next player. */
    public void endTurn() {
        check(canEndTurn(), "The turn can't end now");
        int owner = turn.owner();

        Figure civetTarget = turn.civetTarget;
        if (civetTarget != null) {
            boolean won = turn.ownerRoll.figure().fulfils(civetTarget);
            int points = won ? credit(owner, turn.civetStake, true) : debit(owner, turn.civetStake);
            log(won ? GameEvent.Type.CIVET_WON : GameEvent.Type.CIVET_LOST,
                    owner, points, NOBODY, 0, civetTarget, turn.civetStake);
        }

        Grelottine grelottine = turn.grelottine;
        if (grelottine != null) {
            settleGrelottine(grelottine);
        }

        // A player can only reach 343 points during their own turn
        if (players.get(owner).score >= GOAL) {
            winner = owner;
            log(GameEvent.Type.WINNER, owner, 0);
        }
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (!player.eliminated && player.score <= ELIMINATION) {
                player.eliminated = true;
                log(GameEvent.Type.ELIMINATED, i, 0);
            }
        }
        List<Integer> remaining = otherPlayers();
        if (winner == NOBODY && remaining.size() <= 1) {
            // Last one standing, or the best score if everybody left at once
            winner = remaining.isEmpty() ? players.indexOf(ranking().get(0)) : remaining.get(0);
            log(GameEvent.Type.WINNER, winner, 0);
        }

        List<Player> standings = new ArrayList<>();
        for (Player player : players) {
            standings.add(player.copy());
        }
        history.add(new TurnRecord(turn.number(), owner, new ArrayList<>(turn.events), standings));
        if (winner == NOBODY) {
            turn = new Turn(turn.number() + 1, nextPlayer(owner));
        }
    }

    // ---------------------------------------------------------------- Internals

    private void settleGrelottine(Grelottine grelottine) {
        int target = grelottine.target();
        int challenger = grelottine.challenger();
        int stake = grelottine.stake();
        boolean success = turn.roll.figure().fulfils(grelottine.figure());
        int targetPoints;
        int challengerPoints;
        if (success) {
            targetPoints = credit(target, stake, true);
            challengerPoints = debit(challenger, stake);
        } else {
            challengerPoints = credit(challenger, stake, true);
            targetPoints = debit(target, stake);
        }
        log(success ? GameEvent.Type.GRELOTTINE_WON : GameEvent.Type.GRELOTTINE_LOST,
                target, targetPoints, challenger, challengerPoints, grelottine.figure(), stake);

        // Winning a challenge gives an immunity, once per game
        int winnerOfChallenge = success ? target : challenger;
        Player player = players.get(winnerOfChallenge);
        if (player.passeGrelot == Player.PasseGrelot.NONE) {
            player.passeGrelot = Player.PasseGrelot.AVAILABLE;
            log(GameEvent.Type.PASSE_GRELOT_GAINED, winnerOfChallenge, 0);
        }

        for (int bettor : grelottine.bettors()) {
            settleBet(bettor, success, GameEvent.Type.GRELOTTINE_BET_WON, GameEvent.Type.GRELOTTINE_BET_LOST, 0);
        }
    }

    private void settleBet(int bettor, boolean won, GameEvent.Type wonType, GameEvent.Type lostType, int value) {
        Player player = players.get(bettor);
        int before = player.score;
        player.score -= BET_COST;
        if (won) {
            credit(bettor, BET_PAYOUT, true);
        }
        log(won ? wonType : lostType, bettor, player.score - before, NOBODY, 0, null, value);
    }

    private void bevue(int player, GameEvent.Type type) {
        log(type, player, debit(player, BEVUE_PENALTY));
    }

    /**
     * Adds points to a score, within the end of game caps: points won through a challenge or a bet
     * can't take a score above 332, and only the owner of the turn can reach 343.
     *
     * @return the points actually added
     */
    private int credit(int index, int points, boolean challengeOrBet) {
        Player player = players.get(index);
        int cap = challengeOrBet ? CHALLENGE_CAP : index == turn.owner() ? Integer.MAX_VALUE : GOAL - 1;
        int before = player.score;
        if (before < cap) {
            player.score = (int) Math.min((long) before + points, cap);
        }
        return player.score - before;
    }

    /** @return the points removed, as a negative number */
    private int debit(int index, int points) {
        players.get(index).score -= points;
        return -points;
    }

    private boolean allScoresPositive() {
        for (Player player : players) {
            if (!player.eliminated && player.score < 0) {
                return false;
            }
        }
        return true;
    }

    private int nextPlayer(int from) {
        int next = from;
        do {
            next = (next + 1) % players.size();
        } while (!isActive(next));
        return next;
    }

    private void log(GameEvent.Type type, int player, int points) {
        log(type, player, points, NOBODY, 0, null, 0);
    }

    private void log(GameEvent.Type type, int player, int points, int other, int otherPoints,
                     @Nullable Figure figure, int value) {
        turn.events.add(new GameEvent(type, player, points, other, otherPoints, figure, value));
    }

    private static boolean contains(int[] values, int value) {
        for (int v : values) {
            if (v == value) {
                return true;
            }
        }
        return false;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
