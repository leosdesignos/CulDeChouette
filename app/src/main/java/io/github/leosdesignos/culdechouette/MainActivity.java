package io.github.leosdesignos.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import io.github.leosdesignos.culdechouette.game.Figure;
import io.github.leosdesignos.culdechouette.game.Game;
import io.github.leosdesignos.culdechouette.game.GameEvent;
import io.github.leosdesignos.culdechouette.game.Grelottine;
import io.github.leosdesignos.culdechouette.game.Player;
import io.github.leosdesignos.culdechouette.game.Roll;
import io.github.leosdesignos.culdechouette.game.Turn;

import java.util.ArrayList;
import java.util.List;

/** The game screen. Everything shown is derived from the game, so it survives being recreated. */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_CIVET_OPEN = "civetOpen";
    private static final String STATE_ROLL_KEY = "rollKey";

    private final Figure[] civetFigures = Figure.bettable();

    private DiceEditText dice1EditText;
    private DiceEditText dice2EditText;
    private DiceEditText dice3EditText;
    private TextView statusText;
    private TextView civetInfoText;
    private TextView figureText;
    private TextView playersScoreText;
    private TextView winnerText;
    private TextView rankText;
    private Button validateRollButton;
    private Button siroterButton;
    private Button chouetteVeluteButton;
    private Button suiteButton;
    private Button souffletteButton;
    private Button grelottineButton;
    private Button civetButton;
    private Button nextTurnButton;
    private EditText civetBet;
    private Spinner civetFigure;
    private LinearLayout civetLayout;
    private LinearLayout gameLayout;
    private LinearLayout winnerLayout;

    private GameStore store;
    /** Set while another screen is being opened, so a double tap doesn't open it twice. */
    private boolean opening;
    private boolean civetOpen;
    /** Identifies who is about to throw the dice, to clear the inputs when it changes. */
    private int rollKey = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        store = GameStore.get(this);
        if (GameStore.gameOrRestart(this) == null) {
            return;
        }

        dice1EditText = findViewById(R.id.dice1);
        dice2EditText = findViewById(R.id.dice2);
        dice3EditText = findViewById(R.id.dice3);
        statusText = findViewById(R.id.statusText);
        civetInfoText = findViewById(R.id.civetInfoText);
        figureText = findViewById(R.id.figureText);
        playersScoreText = findViewById(R.id.playersScoreText);
        winnerText = findViewById(R.id.winnerText);
        rankText = findViewById(R.id.rankText);
        validateRollButton = findViewById(R.id.validateRollButton);
        siroterButton = findViewById(R.id.siroterButton);
        chouetteVeluteButton = findViewById(R.id.chouetteVeluteButton);
        suiteButton = findViewById(R.id.suiteButton);
        souffletteButton = findViewById(R.id.souffletteButton);
        grelottineButton = findViewById(R.id.grelottineButton);
        civetButton = findViewById(R.id.civetButton);
        nextTurnButton = findViewById(R.id.nextTurnButton);
        civetBet = findViewById(R.id.civetBet);
        civetFigure = findViewById(R.id.civetFigure);
        civetLayout = findViewById(R.id.civetLayout);
        gameLayout = findViewById(R.id.gameLayout);
        winnerLayout = findViewById(R.id.winnerLayout);

        dice1EditText.jumpTo(dice2EditText);
        dice2EditText.jumpTo(dice3EditText);
        civetFigure.setAdapter(Ui.adapter(this, Texts.figures(this, civetFigures)));
        Ui.clamp(civetBet, () -> game().maxCivetStake());

        validateRollButton.setOnClickListener(v -> validateRoll());
        siroterButton.setOnClickListener(v -> open(SiroterActivity.class));
        chouetteVeluteButton.setOnClickListener(v -> open(ChouetteVeluteActivity.class));
        suiteButton.setOnClickListener(v -> open(SuiteActivity.class));
        souffletteButton.setOnClickListener(v -> open(SouffletteActivity.class));
        grelottineButton.setOnClickListener(v -> showGrelottine());
        civetButton.setOnClickListener(v -> toggleCivet());
        nextTurnButton.setOnClickListener(v -> nextTurn());
        findViewById(R.id.summaryButton).setOnClickListener(v -> open(SummaryActivity.class));
        findViewById(R.id.newGameButton).setOnClickListener(v -> newGame());

        if (savedInstanceState != null) {
            civetOpen = savedInstanceState.getBoolean(STATE_CIVET_OPEN);
            rollKey = savedInstanceState.getInt(STATE_ROLL_KEY, -1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        opening = false;
        if (GameStore.gameOrRestart(this) != null) {
            render();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_CIVET_OPEN, civetOpen);
        outState.putInt(STATE_ROLL_KEY, rollKey);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_undo).setEnabled(store.canUndo());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_summary) {
            open(SummaryActivity.class);
            return true;
        }
        if (id == R.id.action_undo) {
            undo();
            return true;
        }
        if (id == R.id.action_new_game) {
            confirmNewGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Game game() {
        return store.game();
    }

    private void render() {
        Game game = game();
        invalidateOptionsMenu();
        if (game.isOver()) {
            renderWinner(game);
            return;
        }
        gameLayout.setVisibility(View.VISIBLE);
        winnerLayout.setVisibility(View.GONE);

        Turn turn = game.turn();
        Grelottine grelottine = turn.grelottine();
        Roll roll = turn.roll();
        String roller = game.player(game.roller()).name();

        int key = 2 * turn.number() + (grelottine != null ? 1 : 0);
        if (key != rollKey) {
            rollKey = key;
            dice1EditText.setText("");
            dice2EditText.setText("");
            dice3EditText.setText("");
            civetOpen = false;
            civetBet.setText("");
        }

        statusText.setText(grelottine == null
                ? getString(R.string.playing, roller)
                : getString(R.string.playing_grelottine, roller,
                        game.player(grelottine.challenger()).name(), Texts.figure(this, grelottine.figure())));

        Figure civetTarget = turn.civetTarget();
        civetInfoText.setVisibility(civetTarget != null ? View.VISIBLE : View.GONE);
        if (civetTarget != null) {
            civetInfoText.setText(getString(R.string.civet_announced, turn.civetStake(), Texts.figure(this, civetTarget)));
        }
        civetButton.setVisibility(grelottine == null && roll == null && civetTarget == null ? View.VISIBLE : View.GONE);
        civetOpen &= game.canDeclareCivet();
        civetLayout.setVisibility(civetOpen ? View.VISIBLE : View.GONE);
        civetBet.setHint(getString(R.string.stake_max, game.maxCivetStake()));

        boolean canRoll = game.canRoll();
        dice1EditText.setEnabled(canRoll);
        dice2EditText.setEnabled(canRoll);
        dice3EditText.setEnabled(canRoll);
        validateRollButton.setEnabled(canRoll);
        figureText.setText(getString(R.string.figure,
                roll != null ? Texts.roll(this, roll) : getString(R.string.no_figure)));

        siroterButton.setVisibility(game.canSirote() ? View.VISIBLE : View.GONE);
        chouetteVeluteButton.setVisibility(turn.isChouetteVelutePending() ? View.VISIBLE : View.GONE);
        suiteButton.setVisibility(turn.isSuitePending() ? View.VISIBLE : View.GONE);
        souffletteButton.setVisibility(game.canSoufflette() ? View.VISIBLE : View.GONE);
        boolean neant = roll != null && roll.figure() == Figure.NEANT
                && grelottine == null && !turn.isGrelottineCancelled();
        grelottineButton.setVisibility(neant ? View.VISIBLE : View.GONE);
        nextTurnButton.setEnabled(game.canEndTurn());

        StringBuilder scores = new StringBuilder();
        List<Player> players = game.players();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            int points = turn.pointsOf(i);
            String line;
            if (player.isEliminated()) {
                line = getString(R.string.eliminated_line, player.name());
            } else if (points != 0) {
                line = getString(R.string.score_line_turn, player.name(), player.score(), Texts.points(points));
            } else {
                line = getString(R.string.score_line, player.name(), player.score());
            }
            if (scores.length() > 0) {
                scores.append('\n');
            }
            scores.append(line);
        }
        playersScoreText.setText(scores);
    }

    private void renderWinner(Game game) {
        dice1EditText.unfocus();
        gameLayout.setVisibility(View.GONE);
        winnerLayout.setVisibility(View.VISIBLE);
        winnerText.setText(game.player(game.winner()).name());
        StringBuilder rank = new StringBuilder();
        for (Player player : game.ranking()) {
            if (rank.length() > 0) {
                rank.append('\n');
            }
            rank.append(getString(R.string.score_line, player.name(), player.score()));
        }
        rankText.setText(rank);
    }

    private void validateRoll() {
        if (!game().canRoll()) {
            return;
        }
        if (dice1EditText.isEmpty() || dice2EditText.isEmpty() || dice3EditText.isEmpty()) {
            Ui.toast(this, R.string.wrong_dice_input);
            return;
        }
        int dice1 = dice1EditText.value();
        int dice2 = dice2EditText.value();
        int dice3 = dice3EditText.value();
        int civetStake = civetLayout.getVisibility() == View.VISIBLE ? Ui.intValue(civetBet) : 0;
        Figure civetTarget = civetFigures[civetFigure.getSelectedItemPosition()];
        String owner = game().player(game().turn().owner()).name();

        Game.Outcome civetOutcome;
        try {
            civetOutcome = store.perform(game -> {
                Game.Outcome outcome = civetStake > 0 ? game.declareCivet(civetTarget, civetStake) : null;
                game.roll(dice1, dice2, dice3);
                return outcome;
            });
        } catch (IllegalArgumentException | IllegalStateException e) {
            Ui.toast(this, R.string.wrong_bet_input);
            render();
            return;
        }
        if (civetOutcome == Game.Outcome.BEVUE) {
            Ui.toast(this, R.string.bevue_civet, owner);
        }
        List<GameEvent> events = game().turn().events();
        GameEvent last = events.get(events.size() - 1);
        if (last.type() == GameEvent.Type.GRELOTTINE_GAINED) {
            Ui.toast(this, R.string.grelottine_acq, game().player(last.player()).name());
        }

        civetOpen = false;
        civetBet.setText("");
        dice3EditText.unfocus();
        render();

        Turn turn = game().turn();
        if (turn.isChouetteVelutePending()) {
            open(ChouetteVeluteActivity.class);
        } else if (turn.isSuitePending()) {
            open(SuiteActivity.class);
        }
    }

    private void nextTurn() {
        if (!game().canEndTurn()) {
            return;
        }
        store.run(Game::endTurn);
        render();
        if (!game().isOver()) {
            dice1EditText.focus();
        }
    }

    private void toggleCivet() {
        if (!game().canDeclareCivet()) {
            Ui.toast(this, R.string.civet_score);
            return;
        }
        civetOpen = !civetOpen;
        render();
    }

    private void showGrelottine() {
        if (!game().canGrelottine()) {
            Ui.toast(this, R.string.grelottine_scores);
            return;
        }
        open(GrelottineActivity.class);
    }

    private void undo() {
        if (!store.canUndo()) {
            return;
        }
        store.undo();
        Ui.toast(this, R.string.undone);
        render();
    }

    private void confirmNewGame() {
        if (game().isOver()) {
            newGame();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game)
                .setMessage(R.string.new_game_message)
                .setPositiveButton(R.string.new_game, (dialog, which) -> newGame())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void newGame() {
        ArrayList<String> names = new ArrayList<>();
        for (Player player : game().players()) {
            names.add(player.name());
        }
        store.clear();
        startActivity(new Intent(this, StartActivity.class).putStringArrayListExtra(StartActivity.EXTRA_NAMES, names));
        finish();
    }

    private void open(Class<?> activity) {
        if (opening) {
            return;
        }
        opening = true;
        startActivity(new Intent(this, activity));
    }
}
