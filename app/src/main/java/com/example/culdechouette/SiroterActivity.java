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

    private DiceEditText diceEditText;
    private Button validateBetButton;
    private Button validateScoreButton;
    private LinearLayout contreSiropLayout;
    private Spinner playerSpinner;

    private boolean civet;
    private int chouetteValue;
    private HashMap<Player, Integer> siroterScore;
    private PlayersAdapter playersAdapter;
    private GameData game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);

        ListView playersListView = findViewById(R.id.playersListView);
        diceEditText = findViewById(R.id.diceEditText);
        validateBetButton = findViewById(R.id.validateBetButton);
        validateScoreButton = findViewById(R.id.validateScoreButton);
        contreSiropLayout = findViewById(R.id.contreSiropLayout);
        playerSpinner = findViewById(R.id.playerSpinner);
        Button backButton = findViewById(R.id.backButton);

        // Récupérer les données passées à l'activité
        chouetteValue = getIntent().getIntExtra("chouetteValue", -1);

        game = GameData.getInstance();
        siroterScore = new HashMap<>(game.roundScore());

        playersAdapter = new PlayersAdapter(game.playerList());
        playersListView.setAdapter(playersAdapter);

        validateBetButton.setOnClickListener(v -> validateBets());
        validateScoreButton.setOnClickListener(v -> validateScore());
        backButton.setOnClickListener(v -> validateContreSirop());

        ArrayList<Player> playerItems = new ArrayList<>(game.playerList());
        playerItems.add(0, new Player(getString(R.string.no_one)));

        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);
    }

    private void validateBets() {
        // TODO disable spinners
        validateBetButton.setEnabled(false);
        diceEditText.setVisibility(View.VISIBLE);
        validateScoreButton.setVisibility(View.VISIBLE);
        diceEditText.focus();
    }

    private void validateScore() {
        if (diceEditText.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
            return;
        }
        int diceValue = diceEditText.value();

        civet = false;
        boolean contreSirop = true;

        Player currentPlayer = game.currentPlayer();
        for (Player player : game.playerList()) {
            //noinspection ConstantConditions
            int score = siroterScore.containsKey(player) ? siroterScore.get(player) : 0;

            if (player.equals(currentPlayer)) {
                if (diceValue == chouetteValue) {
                    contreSirop = false;
                    score = Roll.roll(chouetteValue, chouetteValue, chouetteValue).figureScore();
                } else {
                    score = -score;
                    if (chouetteValue == 6) {
                        civet = true;
                    }
                }
            }

            int bet = playersAdapter.getPlayerChoice(player);
            if (bet != 0) {
                score -= 5;
                if (bet == diceValue) {
                    score += 30;
                }
            }
            siroterScore.put(player, score);
        }

        if (contreSirop) {
            validateScoreButton.setEnabled(false);
            contreSiropLayout.setVisibility(View.VISIBLE);
        } else {
            backToMainActivity();
        }
    }

    private void validateContreSirop() {
        Player player = (Player) playerSpinner.getSelectedItem();
        if (!player.name().equals(getString(R.string.no_one))) {
            //noinspection ConstantConditions
            int score = siroterScore.get(player);
            score += Roll.roll(chouetteValue, chouetteValue, chouetteValue).figureScore() / 5;
            siroterScore.put(player, score);
        }
        backToMainActivity();
    }

    private void backToMainActivity() {
        game.roundScore().putAll(siroterScore);
        if (civet) {
            game.currentPlayer().setCivet(true);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class PlayersAdapter extends ArrayAdapter<Player> {
        private final Map<Player, Integer> playerSelection;
        private final ArrayAdapter<CharSequence> spinnerAdapter;

        public PlayersAdapter(ArrayList<Player> playersList) {
            super(SiroterActivity.this, R.layout.player_item, playersList);
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
                public void onNothingSelected(AdapterView<?> adapterView) {
                    playerSelection.put(player, 0);
                }
            });

            return convertView;
        }

        public int getPlayerChoice(Player player) {
            Integer choice = playerSelection.get(player);
            return (choice != null) ? choice : 0;
        }
    }
}
