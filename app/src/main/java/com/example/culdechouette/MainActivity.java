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
    private EditText dice1EditText;
    private EditText dice2EditText;
    private EditText dice3EditText;
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

        ArrayList<String> playerNames = getIntent().getStringArrayListExtra("playerNameList");
        for (String name : playerNames) {
            playerList.add(new Player(name));
        }

        validateScoreButton.setOnClickListener(v -> validateScore());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());

        setupDiceEditTexts();
        updatePlayer();
    }

    private void validateScore() {
        if (!checkDiceFields()) {
            Toast.makeText(getApplicationContext(), "Dés non valides", Toast.LENGTH_SHORT).show();
            return;
        }
        int dice1 = Integer.parseInt(dice1EditText.getText().toString());
        int dice2 = Integer.parseInt(dice2EditText.getText().toString());
        int dice3 = Integer.parseInt(dice3EditText.getText().toString());

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
        siroterButton.setVisibility(View.GONE);

        dice1EditText.requestFocus();
        showKeyboard(dice1EditText);
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



    private void setupDiceEditTexts() {
//        dice1EditText.addTextChangedListener(new DiceTextWatcher(dice1EditText, dice2EditText));
//        dice2EditText.addTextChangedListener(new DiceTextWatcher(dice2EditText, dice3EditText));
//        dice3EditText.addTextChangedListener(new DiceTextWatcher(dice3EditText, null));

        dice1EditText.setOnKeyListener(new DiceKeyListener(dice2EditText));
        dice2EditText.setOnKeyListener(new DiceKeyListener(dice3EditText));
        dice3EditText.setOnKeyListener(new DiceKeyListener(null));
    }

    private boolean checkDiceFields() {
        return !dice1EditText.getText().toString().isEmpty() &&
                !dice2EditText.getText().toString().isEmpty() &&
                !dice3EditText.getText().toString().isEmpty();
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class DiceKeyListener implements View.OnKeyListener {

        private EditText nextEditText;
        private Set<Integer> allowedKeys = new HashSet<>(Arrays.asList(
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3,
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_6
        ));

        public DiceKeyListener(EditText nextEditText) {
            this.nextEditText = nextEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (!allowedKeys.contains(keyCode) && keyCode != KeyEvent.KEYCODE_DEL) {
                    return true;
                }
                if (keyCode != KeyEvent.KEYCODE_DEL) {
                    if (nextEditText != null) {
                        nextEditText.requestFocus();
                    } else {
                        hideKeyboard(v);
                    }
                }
            }
            return false;
        }
    }

}
