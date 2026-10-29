package com.example.culdechouette;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.culdechouette.game.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The roller throws one die to turn their Chouette into a Cul de Chouette, the others bet on it. */
public class SiroterActivity extends AppCompatActivity {

    private static final String STATE_BETS = "bets";
    private static final String STATE_LOCKED = "betsLocked";

    private final List<Integer> bettors = new ArrayList<>();
    private final List<Spinner> betSpinners = new ArrayList<>();

    private DiceEditText diceEditText;
    private Button validateBetButton;
    private Button validateButton;
    private TextView resultText;
    private LinearLayout contreSiropLayout;
    private Ui.PlayerSpinner contreSiropSpinner;

    private GameStore store;
    private int roller;
    private int chouetteValue;
    private boolean betsLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);
        store = GameStore.get(this);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }
        if (!game.canSirote()) {
            finish();
            return;
        }
        roller = game.roller();
        chouetteValue = game.turn().roll().value();

        LinearLayout betsLayout = findViewById(R.id.betsLayout);
        diceEditText = findViewById(R.id.diceEditText);
        validateBetButton = findViewById(R.id.validateBetButton);
        validateButton = findViewById(R.id.validateButton);
        resultText = findViewById(R.id.resultText);
        contreSiropLayout = findViewById(R.id.contreSiropLayout);

        ArrayAdapter<String> diceAdapter = Ui.adapter(this,
                Arrays.asList(getResources().getStringArray(R.array.dice_options)));
        for (int player : game.otherPlayers(roller)) {
            if (!game.canBet(player)) {
                continue;
            }
            View row = getLayoutInflater().inflate(R.layout.player_item, betsLayout, false);
            ((TextView) row.findViewById(R.id.playerNameTextView)).setText(game.player(player).name());
            Spinner spinner = row.findViewById(R.id.choiceSpinner);
            spinner.setAdapter(diceAdapter);
            betsLayout.addView(row);
            bettors.add(player);
            betSpinners.add(spinner);
        }
        findViewById(R.id.noBettorText).setVisibility(bettors.isEmpty() ? View.VISIBLE : View.GONE);
        contreSiropSpinner = new Ui.PlayerSpinner(findViewById(R.id.playerSpinner), game, game.otherPlayers(roller), true);

        if (savedInstanceState != null) {
            betsLocked = savedInstanceState.getBoolean(STATE_LOCKED);
            int[] bets = savedInstanceState.getIntArray(STATE_BETS);
            for (int i = 0; bets != null && i < bets.length && i < betSpinners.size(); i++) {
                betSpinners.get(i).setSelection(bets[i]);
            }
        }

        validateBetButton.setOnClickListener(v -> lockBets());
        validateButton.setOnClickListener(v -> validate());
        diceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                updateResult();
            }
        });
        applyLock();
        updateResult();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_LOCKED, betsLocked);
        int[] bets = new int[betSpinners.size()];
        for (int i = 0; i < bets.length; i++) {
            bets[i] = betSpinners.get(i).getSelectedItemPosition();
        }
        outState.putIntArray(STATE_BETS, bets);
    }

    private void lockBets() {
        betsLocked = true;
        applyLock();
        diceEditText.focus();
    }

    /** Bets are placed before the die is thrown. */
    private void applyLock() {
        for (Spinner spinner : betSpinners) {
            spinner.setEnabled(!betsLocked);
        }
        validateBetButton.setEnabled(!betsLocked);
        diceEditText.setVisibility(betsLocked ? View.VISIBLE : View.GONE);
        validateButton.setVisibility(betsLocked ? View.VISIBLE : View.GONE);
    }

    private void updateResult() {
        int die = diceEditText.value();
        if (die == 0) {
            resultText.setVisibility(View.GONE);
            contreSiropLayout.setVisibility(View.GONE);
            return;
        }
        String name = store.game().player(roller).name();
        boolean success = die == chouetteValue;
        resultText.setVisibility(View.VISIBLE);
        resultText.setText(success
                ? getString(R.string.sirotage_success, name, chouetteValue)
                : getString(R.string.sirotage_fail, name));
        contreSiropLayout.setVisibility(success ? View.GONE : View.VISIBLE);
    }

    private void validate() {
        int die = diceEditText.value();
        if (die == 0) {
            Ui.toast(this, R.string.wrong_dice_input);
            return;
        }
        Game game = store.game();
        // The position in the spinner is the value of the die bet on, 0 for no bet
        int[] bets = new int[game.players().size()];
        for (int i = 0; i < bettors.size(); i++) {
            bets[bettors.get(i)] = betSpinners.get(i).getSelectedItemPosition();
        }
        int contreSirop = die == chouetteValue ? Game.NOBODY : contreSiropSpinner.selected();
        boolean hadCivet = game.player(roller).hasCivet();

        store.run(g -> g.sirote(die, bets, contreSirop));

        if (!hadCivet && store.game().player(roller).hasCivet()) {
            Ui.toast(this, R.string.civet_acq, store.game().player(roller).name());
        }
        finish();
    }
}
