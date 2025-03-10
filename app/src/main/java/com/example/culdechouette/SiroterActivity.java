package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

    private TextView siroterTitle;
    private TextView siroterMessage;
    private ListView playersListView;
    private DiceEditText diceEditText;
    private Button validateBetButton;
    private Button validateScoreButton;

    private int chouetteValue;
    private int currentPlayerIndex;
    private ArrayList<Player> playerList;
    private Map<String, Integer> roundScore;
    private Map<String, Spinner> playerSpinners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);

        siroterTitle = findViewById(R.id.siroterTitle);
        siroterMessage = findViewById(R.id.siroterMessage);
        playersListView = findViewById(R.id.playersListView);
        diceEditText = findViewById(R.id.diceEditText);
        validateBetButton = findViewById(R.id.validateBetButton);
        validateScoreButton = findViewById(R.id.validateScoreButton);

        // Récupérer les données passées à l'activité
        chouetteValue = getIntent().getIntExtra("chouetteValue", -1);
        currentPlayerIndex = getIntent().getIntExtra("currentPlayerIndex", -1);
        playerList = (ArrayList<Player>) getIntent().getSerializableExtra("playerList");
        roundScore = (HashMap<String, Integer>) getIntent().getSerializableExtra("roundScore");

        playerSpinners = new HashMap<>();

        PlayersAdapter adapter = new PlayersAdapter(playerList);
        playersListView.setAdapter(adapter);

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
            Toast.makeText(getApplicationContext(), "Dé non valide", Toast.LENGTH_SHORT).show();
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

            int bet = playerSpinners.get(player.name()).getSelectedItemPosition();
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
        private ArrayList<Player> playersList;

        public PlayersAdapter(ArrayList<Player> playersList) {
            super(SiroterActivity.this, R.layout.player_item, playersList);
            this.playersList = playersList;
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

            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.dice_options, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            choiceSpinner.setAdapter(spinnerAdapter);

            playerSpinners.put(player.name(), choiceSpinner);

            return convertView;
        }
    }
}
