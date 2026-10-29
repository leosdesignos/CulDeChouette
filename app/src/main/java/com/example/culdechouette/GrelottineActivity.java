package com.example.culdechouette;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.culdechouette.game.Figure;
import com.example.culdechouette.game.Game;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A player challenges the owner of the turn after their Néant: choice of the challenger,
 * possible Passe-Grelot to another target, then figure, stake and bets.
 */
public class GrelottineActivity extends AppCompatActivity {

    private static final String STATE_STEP = "step";
    private static final String STATE_CHALLENGER = "challenger";
    private static final String STATE_TARGET = "target";
    private static final String STATE_BETTORS = "bettors";

    private static final int STEP_CHALLENGER = 0;
    private static final int STEP_NEW_TARGET = 1;
    private static final int STEP_CHALLENGE = 2;

    private final Figure[] figures = Figure.bettable();
    private final List<MaterialCheckBox> betBoxes = new ArrayList<>();
    private final List<Integer> bettors = new ArrayList<>();

    private TextView challengerLabel;
    private TextView challengeText;
    private TextView passeGrelotToText;
    private TextView betsTitle;
    private Spinner challengerSpinnerView;
    private Spinner newTargetSpinnerView;
    private Spinner grelottineFigure;
    private LinearLayout newTargetLayout;
    private LinearLayout grelottineLayout;
    private LinearLayout betsLayout;
    private Button validateButton;
    private Button passeGrelotButton;
    private Button startButton;
    private EditText grelottineBet;
    private Ui.PlayerSpinner challengerSpinner;
    private Ui.PlayerSpinner newTargetSpinner;

    private GameStore store;
    private int owner;
    private int step = STEP_CHALLENGER;
    private int challenger = Game.NOBODY;
    private int target;
    private boolean[] checkedBettors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grelottine);
        store = GameStore.get(this);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }
        if (!game.canGrelottine()) {
            finish();
            return;
        }
        owner = game.turn().owner();
        target = owner;
        checkedBettors = new boolean[game.players().size()];

        challengerLabel = findViewById(R.id.challengerLabel);
        challengeText = findViewById(R.id.challengeText);
        passeGrelotToText = findViewById(R.id.passeGrelotToText);
        betsTitle = findViewById(R.id.betsTitle);
        challengerSpinnerView = findViewById(R.id.challengerSpinner);
        newTargetSpinnerView = findViewById(R.id.newTargetSpinner);
        grelottineFigure = findViewById(R.id.grelottineFigure);
        newTargetLayout = findViewById(R.id.newTargetLayout);
        grelottineLayout = findViewById(R.id.grelottineLayout);
        betsLayout = findViewById(R.id.betsLayout);
        validateButton = findViewById(R.id.validateButton);
        passeGrelotButton = findViewById(R.id.passeGrelotButton);
        startButton = findViewById(R.id.startButton);
        grelottineBet = findViewById(R.id.grelottineBet);

        challengerSpinner = new Ui.PlayerSpinner(challengerSpinnerView, game, game.otherPlayers(owner), false);
        grelottineFigure.setAdapter(Ui.adapter(this, Texts.figures(this, figures)));
        grelottineFigure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMaxStake();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        Ui.clamp(grelottineBet, this::maxStake);

        if (savedInstanceState != null) {
            step = savedInstanceState.getInt(STATE_STEP);
            challenger = savedInstanceState.getInt(STATE_CHALLENGER);
            target = savedInstanceState.getInt(STATE_TARGET);
            boolean[] checked = savedInstanceState.getBooleanArray(STATE_BETTORS);
            if (checked != null) {
                checkedBettors = checked;
            }
        } else if (game.turn().grelottineChallenger() != Game.NOBODY) {
            // Back to a challenge announced before this screen was left
            challenger = game.turn().grelottineChallenger();
            step = game.turn().passeGrelotBy() != Game.NOBODY ? STEP_NEW_TARGET : STEP_CHALLENGE;
        }

        validateButton.setOnClickListener(v -> validate());
        passeGrelotButton.setOnClickListener(v -> passeGrelot());
        startButton.setOnClickListener(v -> start());
        render();
        if (step == STEP_CHALLENGER && savedInstanceState == null) {
            challengerSpinnerView.post(challengerSpinnerView::performClick);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_STEP, step);
        outState.putInt(STATE_CHALLENGER, challenger);
        outState.putInt(STATE_TARGET, target);
        outState.putBooleanArray(STATE_BETTORS, checkedBettors);
    }

    private Game game() {
        return store.game();
    }

    private String name(int player) {
        return game().player(player).name();
    }

    private void validate() {
        if (isFinishing()) {
            return;
        }
        if (step == STEP_CHALLENGER) {
            int selected = challengerSpinner.selected();
            Game.Outcome outcome = store.tryPerform(game -> game.announceGrelottine(selected));
            if (outcome != Game.Outcome.OK) {
                if (outcome == Game.Outcome.BEVUE) {
                    Ui.toast(this, R.string.bevue_grelottine, name(selected));
                }
                finish();
                return;
            }
            challenger = selected;
            step = STEP_CHALLENGE;
        } else if (step == STEP_NEW_TARGET) {
            target = newTargetSpinner.selected();
            step = STEP_CHALLENGE;
        }
        render();
    }

    private void passeGrelot() {
        if (isFinishing()) {
            return;
        }
        Game.Outcome outcome = store.tryPerform(Game::passeGrelot);
        if (outcome == null || outcome == Game.Outcome.CANCELLED) {
            if (outcome != null) {
                Ui.toast(this, R.string.passe_grelot_cancel);
            }
            finish();
            return;
        }
        if (outcome == Game.Outcome.BEVUE) {
            Ui.toast(this, R.string.bevue_passe_grelot, name(owner));
        } else {
            step = STEP_NEW_TARGET;
        }
        render();
    }

    private void start() {
        if (isFinishing()) {
            return;
        }
        if (grelottineBet.getText().length() == 0) {
            Ui.toast(this, R.string.wrong_bet_input);
            return;
        }
        int stake = Ui.intValue(grelottineBet);
        Figure figure = selectedFigure();
        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < bettors.size(); i++) {
            if (betBoxes.get(i).isChecked()) {
                chosen.add(bettors.get(i));
            }
        }
        int[] bettorIndexes = new int[chosen.size()];
        for (int i = 0; i < bettorIndexes.length; i++) {
            bettorIndexes[i] = chosen.get(i);
        }
        try {
            store.run(game -> game.startGrelottine(target, figure, stake, bettorIndexes));
        } catch (IllegalArgumentException e) {
            Ui.toast(this, R.string.wrong_bet_input);
            return;
        } catch (IllegalStateException e) {
            // Started from another screen in the meantime
        }
        finish();
    }

    private void render() {
        Game game = game();
        boolean chooseChallenger = step == STEP_CHALLENGER;
        boolean challenge = step == STEP_CHALLENGE;

        challengerLabel.setVisibility(chooseChallenger ? View.VISIBLE : View.GONE);
        challengerLabel.setText(getString(R.string.grelottine_challenger, name(owner)));
        challengerSpinnerView.setVisibility(chooseChallenger ? View.VISIBLE : View.GONE);
        challengeText.setVisibility(chooseChallenger ? View.GONE : View.VISIBLE);
        if (!chooseChallenger) {
            challengeText.setText(getString(R.string.challenge_text, name(challenger), name(target)));
        }

        newTargetLayout.setVisibility(step == STEP_NEW_TARGET ? View.VISIBLE : View.GONE);
        if (step == STEP_NEW_TARGET && newTargetSpinner == null) {
            passeGrelotToText.setText(getString(R.string.passe_grelot_to, name(owner)));
            newTargetSpinner = new Ui.PlayerSpinner(newTargetSpinnerView, game,
                    game.otherPlayers(owner, challenger), false);
        }
        validateButton.setVisibility(challenge ? View.GONE : View.VISIBLE);
        validateButton.setText(chooseChallenger ? R.string.validate_challenge : R.string.validate);

        passeGrelotButton.setVisibility(challenge && target == owner && game.canPasseGrelot()
                ? View.VISIBLE : View.GONE);
        grelottineLayout.setVisibility(challenge ? View.VISIBLE : View.GONE);
        startButton.setVisibility(challenge ? View.VISIBLE : View.GONE);
        if (challenge) {
            buildBets(game);
            updateMaxStake();
        } else {
            betsTitle.setVisibility(View.GONE);
            betsLayout.setVisibility(View.GONE);
        }
    }

    /** Players not involved in the challenge can bet on its success. */
    private void buildBets(Game game) {
        betsLayout.removeAllViews();
        betBoxes.clear();
        bettors.clear();
        for (int player : game.otherPlayers(challenger, target)) {
            if (!game.canBet(player)) {
                continue;
            }
            MaterialCheckBox box = new MaterialCheckBox(this);
            box.setText(game.player(player).name());
            box.setChecked(checkedBettors[player]);
            box.setOnCheckedChangeListener((button, checked) -> checkedBettors[player] = checked);
            betsLayout.addView(box);
            betBoxes.add(box);
            bettors.add(player);
        }
        betsTitle.setVisibility(bettors.isEmpty() ? View.GONE : View.VISIBLE);
        betsLayout.setVisibility(bettors.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** The maximum depends on both players and on the figure, so it changes with the target. */
    private void updateMaxStake() {
        if (step != STEP_CHALLENGE) {
            return;
        }
        int max = maxStake();
        grelottineBet.setHint(getString(R.string.stake_max, max));
        if (Ui.intValue(grelottineBet) > max) {
            grelottineBet.setText(String.valueOf(max));
        }
    }

    private int maxStake() {
        return challenger == Game.NOBODY ? 0 : game().maxGrelottineStake(challenger, target, selectedFigure());
    }

    private Figure selectedFigure() {
        return figures[Math.max(0, grelottineFigure.getSelectedItemPosition())];
    }
}
