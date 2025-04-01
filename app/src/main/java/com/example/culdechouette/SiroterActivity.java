package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SiroterActivity extends AppCompatActivity {

    public static final int SUBACT_ID = 0;

    private ListView playersListView;
    private DiceEditText diceEditText;
    private Button validateBetButton;
    private Button validateRollButton;
    private Button backButton;
    private TextView resultText;
    private TextView hintText;
    private LinearLayout contreSiropLayout;
    private Spinner playerSpinner;

    private boolean civet;
    private boolean success;
    private int chouetteValue;
    private HashMap<Player, Integer> siroterScore;
    private PlayersAdapter playersAdapter;
    private GameData game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);

        playersListView = findViewById(R.id.playersListView);
        diceEditText = findViewById(R.id.diceEditText);
        validateBetButton = findViewById(R.id.validateBetButton);
        validateRollButton = findViewById(R.id.validateRollButton);
        resultText = findViewById(R.id.resultText);
        hintText = findViewById(R.id.hintText);
        contreSiropLayout = findViewById(R.id.contreSiropLayout);
        playerSpinner = findViewById(R.id.playerSpinner);
        backButton = findViewById(R.id.backButton);

        // Récupérer les données passées à l'activité
        chouetteValue = getIntent().getIntExtra("chouetteValue", -1);

        game = GameData.getInstance();
        siroterScore = new HashMap<>();

        ArrayList<Player> otherPlayerList = new ArrayList<>(game.playerList());
        otherPlayerList.remove(game.currentPlayer());
        playersAdapter = new PlayersAdapter(otherPlayerList);
        playersListView.setAdapter(playersAdapter);

        validateBetButton.setOnClickListener(v -> validateBets());
        validateRollButton.setOnClickListener(v -> validateRoll());
        backButton.setOnClickListener(v -> backToMainActivity());

        ArrayList<Player> playerItems = new ArrayList<>(otherPlayerList);
        playerItems.add(0, new Player(getString(R.string.no_one)));

        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);
    }

    private void validateBets() {
        // Avoid going out of screen with lot of player
        if (playersAdapter.getCount() > 3) {
            View item = playersAdapter.getView(0, null, playersListView);
            item.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int itemHeight = item.getMeasuredHeight();

            ViewGroup.LayoutParams params = playersListView.getLayoutParams();
            params.height = (int) (3.5 * itemHeight);
            playersListView.setLayoutParams(params);
        }

        playersAdapter.disableSpinners();
        validateBetButton.setEnabled(false);
        diceEditText.setVisibility(View.VISIBLE);
        validateRollButton.setVisibility(View.VISIBLE);
        diceEditText.focus();
    }

    private void validateRoll() {
        if (diceEditText.isEmpty()) {
            Toast.makeText(this, R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
            return;
        }
        int diceValue = diceEditText.value();

        civet = false;
        success = false;

        Player currentPlayer = game.currentPlayer();
        for (Player player : game.playerList()) {
            int score = 0;
            if (player.equals(currentPlayer)) {
                int chouetteScore = new Roll(Roll.Figure.CHOUETTE, chouetteValue).figureScore();
                score -= chouetteScore; // Revert score added in MainActivity
                if (diceValue == chouetteValue) {
                    success = true;
                    score += new Roll(Roll.Figure.CUL_DE_CHOUETTE_SIROTE, chouetteValue).figureScore();
                } else {
                    score -= chouetteScore;
                    if (chouetteValue == 6) {
                        civet = true;
                    }
                }
            } else {
                int bet = playersAdapter.getPlayerChoice(player);
                if (bet != 0) {
                    score -= 5;
                    if (bet == diceValue) {
                        score += 30;
                    }
                }
            }
            siroterScore.put(player, score);
        }

        hintText.setVisibility(View.GONE);
        validateRollButton.setEnabled(false);
        backButton.setEnabled(true);

        resultText.setVisibility(View.VISIBLE);
        resultText.setText(String.format("%s%s%s",
                currentPlayer.name(),
                success ? getString(R.string.sirotage_success) : getString(R.string.sirotage_fail),
                success ? new Roll(Roll.Figure.CHOUETTE, chouetteValue).figureName() : ""));

        if (!success) {
            contreSiropLayout.setVisibility(View.VISIBLE);
            playerSpinner.performClick();
        }
    }

    private void backToMainActivity() {
        if (!success) {
            Player player = (Player) playerSpinner.getSelectedItem();
            if (!player.name().equals(getString(R.string.no_one))) {
                game.addRoundScore(player, new Roll(Roll.Figure.CUL_DE_CHOUETTE_SIROTE, chouetteValue).figureScore() / 5);
            }
        }
        for (Player player : siroterScore.keySet()) {
            //noinspection ConstantConditions
            game.addRoundScore(player, siroterScore.get(player));
        }
        if (civet) {
            if (game.currentPlayer().setCivet(true)) {
                Toast.makeText(this, R.string.civet_acq, Toast.LENGTH_SHORT).show();
            }
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        resultIntent.putExtra("success", success);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class PlayersAdapter extends ArrayAdapter<Player> {

        private final Map<Player, Integer> playerSelection;
        private final ArrayAdapter<CharSequence> spinnerAdapter;

        private boolean spinnerEnable;

        public PlayersAdapter(ArrayList<Player> playersList) {
            super(SiroterActivity.this, R.layout.player_item, playersList);
            this.spinnerEnable = true;
            this.playerSelection = new HashMap<>();
            this.spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.dice_options, android.R.layout.simple_spinner_item);
            this.spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.player_item, parent, false);
            }

            TextView playerNameTextView = convertView.findViewById(R.id.playerNameTextView);
            Spinner diceSpinner = convertView.findViewById(R.id.choiceSpinner);

            Player player = getItem(position);
            playerNameTextView.setText(player.name());

            diceSpinner.setAdapter(spinnerAdapter);
            diceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerSelection.put(player, position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });
            if (playerSelection.containsKey(player)) {
                //noinspection ConstantConditions
                diceSpinner.setSelection(playerSelection.get(player));
            }
            diceSpinner.setEnabled(spinnerEnable);

            return convertView;
        }

        public int getPlayerChoice(Player player) {
            Integer choice = playerSelection.get(player);
            return (choice != null) ? choice : 0;
        }

        public void disableSpinners() {
            this.spinnerEnable = false;
            notifyDataSetChanged();
        }
    }
}
