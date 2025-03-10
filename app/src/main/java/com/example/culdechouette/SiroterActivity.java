package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private int chouetteValue;
    private int currentPlayerIndex;
    private ArrayList<Player> playerList;
    private Map<String, Integer> roundScore;
    private PlayersAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);

        ListView playersListView = findViewById(R.id.playersListView);
        diceEditText = findViewById(R.id.diceEditText);
        validateBetButton = findViewById(R.id.validateBetButton);
        validateScoreButton = findViewById(R.id.validateScoreButton);

        // Récupérer les données passées à l'activité
        chouetteValue = getIntent().getIntExtra("chouetteValue", -1);
        currentPlayerIndex = getIntent().getIntExtra("currentPlayerIndex", -1);
        playerList = (ArrayList<Player>) getIntent().getSerializableExtra("playerList");
        roundScore = (HashMap<String, Integer>) getIntent().getSerializableExtra("roundScore");

        playersAdapter = new PlayersAdapter(playerList);
        playersListView.setAdapter(playersAdapter);

        validateBetButton.setOnClickListener(v -> validateBets());
        validateScoreButton.setOnClickListener(v -> validateScore());
    }

    private void validateBets() {
        validateBetButton.setEnabled(false);
        diceEditText.setVisibility(View.VISIBLE);
        validateScoreButton.setVisibility(View.VISIBLE);
    }

    private void validateScore() {
        if (diceEditText.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
            return;
        }
        int diceValue = diceEditText.value();

        boolean civet = false;
        Player currentPlayer = playerList.get(currentPlayerIndex);
        for (Player player : playerList) {
            int score = 0;
            if (roundScore.containsKey(player.name())) {
                score = roundScore.get(player.name());
            }

            if (player.equals(currentPlayer)) {
                if (diceValue == chouetteValue) {
                    score = Roll.roll(chouetteValue, chouetteValue, chouetteValue).figureScore();
                } else {
                    score = -score;
                    if (chouetteValue == 6) {
                        civet = true;
                    }
                }
            }

            int bet = playersAdapter.getPlayerChoice(player.name());
            if (bet != 0) {
                score -= 5;
                if (bet == diceValue) {
                    score += 30;
                }
            }
            roundScore.put(player.name(), score);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        resultIntent.putExtra("civet", civet);
        resultIntent.putExtra("roundScore", (HashMap<String, Integer>) roundScore);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class PlayersAdapter extends ArrayAdapter<Player> {
        private final Map<String, Integer> playerSelection;
        private final ArrayAdapter<CharSequence> spinnerAdapter;

        public PlayersAdapter(ArrayList<Player> playersList) {
            super(SiroterActivity.this, R.layout.player_item, playersList);
            this.playerSelection = new HashMap<>();
            this.spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.dice_options, android.R.layout.simple_spinner_item);
            this.spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.player_item, parent, false);
            }

            TextView playerNameTextView = convertView.findViewById(R.id.playerNameTextView);
            Spinner choiceSpinner = convertView.findViewById(R.id.choiceSpinner);

            Player player = getItem(position);
            playerNameTextView.setText(player.name());

            choiceSpinner.setAdapter(spinnerAdapter);
            choiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    playerSelection.put(player.name(), position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    playerSelection.put(player.name(), 0);
                }
            });

            return convertView;
        }

        public int getPlayerChoice(String playerName) {
            Integer choice = playerSelection.get(playerName);
            return (choice != null) ? choice : 0;
        }
    }
}
