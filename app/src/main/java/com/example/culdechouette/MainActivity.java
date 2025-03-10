package com.example.culdechouette;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView currentPlayerText;
    private DiceEditText dice1EditText;
    private DiceEditText dice2EditText;
    private DiceEditText dice3EditText;
    private TextView resultText;
    private TextView figureText;
    private TextView playersScoreText;
    private Button validateScoreButton;
    private Button siroterButton;
    private Button nextTurnButton;

    private int currentPlayerIndex = -1;
    private ArrayList<Player> playerList = new ArrayList<>();
    private HashMap<String, Integer> roundScore = new HashMap<>();
    private Roll roll;

    ActivityResultLauncher<Intent> subActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        int id = intent.getIntExtra("id", -1);
                        if (id == SiroterActivity.SUBACT_ID) {
                            siroterButton.setEnabled(false);
                            roundScore = (HashMap<String, Integer>) intent.getSerializableExtra("roundScore");
                            if (intent.getBooleanExtra("civet", false)) {
                                playerList.get(currentPlayerIndex).setCivet(true);
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPlayerText = findViewById(R.id.currentPlayerText);
        dice1EditText = findViewById(R.id.dice1);
        dice2EditText = findViewById(R.id.dice2);
        dice3EditText = findViewById(R.id.dice3);
        resultText = findViewById(R.id.resultText);
        figureText = findViewById(R.id.figureText);
        playersScoreText = findViewById(R.id.playersScoreText);
        validateScoreButton = findViewById(R.id.validateScoreButton);
        siroterButton = findViewById(R.id.siroterButton);
        nextTurnButton = findViewById(R.id.nextTurnButton);

        dice1EditText.jumpTo(dice2EditText);
        dice2EditText.jumpTo(dice3EditText);

        ArrayList<String> playerNames = getIntent().getStringArrayListExtra("playerNameList");
        for (String name : playerNames) {
            playerList.add(new Player(name));
        }

        validateScoreButton.setOnClickListener(v -> validateScore());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());

        updatePlayer();
    }

    private void validateScore() {
        if (dice1EditText.isEmpty() || dice2EditText.isEmpty() || dice3EditText.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Dés non valides", Toast.LENGTH_SHORT).show();
            return;
        }
        int dice1 = dice1EditText.value();
        int dice2 = dice2EditText.value();
        int dice3 = dice3EditText.value();

        roll = Roll.roll(dice1, dice2, dice3);
        figureText.setText(roll.figureName());
        resultText.setText(String.valueOf(roll.figureScore()));

        Player currentPlayer = playerList.get(currentPlayerIndex);
        switch (roll.figure()) {
            case CHOUETTE:
                roundScore.put(currentPlayer.name(), roll.figureScore());
                siroterButton.setVisibility(View.VISIBLE);
                break;
            case CHOUETTE_VELUTE:
                // TODO pts to first pas mou le caillou
                break;
            case CUL_DE_CHOUETTE:
            case CUL_DE_CHOUETTE_SIROTE:
            case VELUTE:
                roundScore.put(currentPlayer.name(), roll.figureScore());
                break;
            case SUITE_VELUTE:
                roundScore.put(currentPlayer.name(), roll.figureScore());
            case SUITE:
                // TODO pts to last grelotte ca picotte
                break;
            case SOUFLETTE:
                break;
            case NEANT:
                currentPlayer.setGrelottine(true);
                break;
            default:
                break;
        }

        nextTurnButton.setEnabled(true);
//        validateScoreButton.setEnabled(false);
    }

    private void nextTurn() {
        updatePlayer();
        updatePlayersScore();

        // Réinitialiser les champs de dés et le score
        dice1EditText.setText("");
        dice2EditText.setText("");
        dice3EditText.setText("");
        resultText.setText("0");
        figureText.setText("Aucune");
//        validateScoreButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        siroterButton.setEnabled(true);
        siroterButton.setVisibility(View.GONE);

        dice1EditText.focus();
    }

    private void updatePlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= playerList.size()) {
            currentPlayerIndex = 0;
        }
        currentPlayerText.setText(playerList.get(currentPlayerIndex).name());
    }

    private void updatePlayersScore() {
        StringBuilder scoresText = new StringBuilder("");
        for (Player player : playerList) {
            if (roundScore.containsKey(player.name())){
                player.addScore(roundScore.get(player.name()));
            }
            scoresText.append(player.name()).append(" : ").append(player.score()).append("\n");
        }
        playersScoreText.setText(scoresText.toString());

        roundScore.clear();
    }

    private void showSiroterPopup() {
        Intent intent = new Intent(this, SiroterActivity.class);
        intent.putExtra("currentPlayerIndex", currentPlayerIndex);
        intent.putExtra("playerList", playerList);
        intent.putExtra("chouetteValue", roll.figureValue());
        intent.putExtra("roundScore", (HashMap<String, Integer>) roundScore);
        subActivity.launch(intent);
    }

}
