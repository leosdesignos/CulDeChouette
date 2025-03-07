package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView currentPlayerText;
    private EditText dice1EditText;
    private EditText dice2EditText;
    private EditText dice3EditText;
    private TextView resultText;
    private TextView figureText;
    private TextView teamsScoreText;
    private Button validateScoreButton;
    private Button siroterButton;
    private Button nextTurnButton;

    private ArrayList<String> playersList;
    private Map<String, Integer> teamScores;
    private Map<String, String> playerTeam;
    private int currentPlayerIndex = -1;

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
        teamsScoreText = findViewById(R.id.teamsScoreText);
        validateScoreButton = findViewById(R.id.validateScoreButton);
        siroterButton = findViewById(R.id.siroterButton);
        nextTurnButton = findViewById(R.id.nextTurnButton);

        playersList = getIntent().getStringArrayListExtra("playersList");
        playerTeam = (HashMap<String, String>) getIntent().getSerializableExtra("playerTeam");
        teamScores = new HashMap<>();

        // Initialiser les scores des équipes à 0
        for (String player : playersList) {
            String teamName = playerTeam.get(player);
            if (!teamScores.containsKey(teamName)) {
                teamScores.put(teamName, 0);
            }
        }

        validateScoreButton.setOnClickListener(v -> validateScore());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());

        setupDiceEditTexts();
        updatePlayer();
    }

    private void validateScore() {
        int dice1 = Integer.parseInt(dice1EditText.getText().toString());
        int dice2 = Integer.parseInt(dice2EditText.getText().toString());
        int dice3 = Integer.parseInt(dice3EditText.getText().toString());

        ScoreResult result = calculateScoreAndGetFigureName(dice1, dice2, dice3);
        resultText.setText(String.valueOf(result.score));
        figureText.setText(result.figureName);

        if (result.figureName.startsWith("Chouette de")) {
            siroterButton.setVisibility(View.VISIBLE);
        }

        nextTurnButton.setEnabled(true);
        validateScoreButton.setEnabled(false);
    }

    private void nextTurn() {
        updatePlayer();
        updateTeamsScore();

        // Réinitialiser les champs de dés et le score
        dice1EditText.setText("");
        dice2EditText.setText("");
        dice3EditText.setText("");
        resultText.setText("0");
        figureText.setText("Aucune");
        validateScoreButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
        siroterButton.setVisibility(View.GONE);

        dice1EditText.requestFocus();
        showKeyboard(dice1EditText);
    }

    private void updatePlayer() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= playersList.size()) {
            currentPlayerIndex = 0;
        }
        currentPlayerText.setText(playersList.get(currentPlayerIndex));
    }

    private void updateTeamsScore() {
        String currentPlayer = playersList.get(currentPlayerIndex);
        String currentTeam = playerTeam.get(currentPlayer);
        int currentTeamScore = teamScores.get(currentTeam);
//        teamScores.put(currentTeam, currentTeamScore + result.score); // TODO cumulate score

        StringBuilder scoresText = new StringBuilder("");
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            scoresText.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        teamsScoreText.setText(scoresText.toString());
    }

    private ScoreResult calculateScoreAndGetFigureName(int dice1, int dice2, int dice3) {
        // Trier les dés pour simplifier les comparaisons
        int[] dice = {dice1, dice2, dice3};
        Arrays.sort(dice);

        // Vérifier Cul de Chouette
        if (dice[0] == dice[1] && dice[1] == dice[2]) {
            int value = dice[0];
            return new ScoreResult(40 + dice[0] * 10, "Cul de Chouette de " + value);
        }

        // Vérifier Chouette Velute
        if (dice[0] == dice[1] && dice[0] + dice[1] == dice[2]) {
            return new ScoreResult(2 * dice[2] * dice[2], "Chouette Velute de " + dice[2]);
        }

        // Vérifier Chouette
        if (dice[0] == dice[1] || dice[1] == dice[2]) {
            return new ScoreResult(dice[1] * dice[1], "Chouette de " + dice[1]);
        }

        // Vérifier Velute
        if (dice[0] + dice[1] == dice[2]) {
            return new ScoreResult(2 * dice[2] * dice[2], "Velute de " + dice[2]);
        }

        // Vérifier Suite
        if (dice[2] == dice[1] + 1 && dice[1] == dice[0] + 1) {
            if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
                return new ScoreResult(18, "Suite Velute");
            }
            return new ScoreResult(-10, "Suite");
        }

        // Vérifier Soufflette
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 4) {
            return new ScoreResult(0, "Soufflette");
        }

        // Si aucune combinaison, c'est un Néant
        return new ScoreResult(0, "Néant");
    }

    private void showSiroterPopup() {
        Intent intent = new Intent(this, SiroterActivity.class);
        intent.putExtra("currentPlayer", playersList.get(currentPlayerIndex));
        intent.putExtra("currentTeam", playerTeam.get(playersList.get(currentPlayerIndex)));
        intent.putExtra("playersList", playersList);
        intent.putExtra("playerTeam", (HashMap<String, String>) playerTeam);
        intent.putExtra("teamScores", (HashMap<String, Integer>) teamScores);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int siroterResult = data.getIntExtra("siroterResult", 0);
            String currentPlayer = playersList.get(currentPlayerIndex);
            String currentTeam = playerTeam.get(currentPlayer);
            int currentTeamScore = teamScores.get(currentTeam);

            if (siroterResult > 0) {
                teamScores.put(currentTeam, currentTeamScore + siroterResult);
            } else {
                teamScores.put(currentTeam, currentTeamScore + siroterResult);
            }

            // TODO
        }
    }

    private void setupDiceEditTexts() {
        dice1EditText.addTextChangedListener(new DiceTextWatcher(dice1EditText, dice2EditText));
        dice2EditText.addTextChangedListener(new DiceTextWatcher(dice2EditText, dice3EditText));
        dice3EditText.addTextChangedListener(new DiceTextWatcher(dice3EditText, null));

//        dice1EditText.setOnKeyListener(new DiceKeyListener(dice2EditText));
//        dice2EditText.setOnKeyListener(new DiceKeyListener(dice3EditText));
//        dice3EditText.setOnKeyListener(new DiceKeyListener(null));
    }

    private void checkValidateButtonStatus() {
        boolean isValid = !dice1EditText.getText().toString().isEmpty() &&
                !dice2EditText.getText().toString().isEmpty() &&
                !dice3EditText.getText().toString().isEmpty();
        validateScoreButton.setEnabled(isValid);
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

    private static class ScoreResult {
        int score;
        String figureName;

        ScoreResult(int score, String figureName) {
            this.score = score;
            this.figureName = figureName;
        }
    }

    private class DiceTextWatcher implements TextWatcher {
        private EditText currentEditText;
        private EditText nextEditText;

        public DiceTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) {
                int value = Integer.parseInt(s.toString());
                if (value >= 1 && value <= 6) {
                    if (nextEditText != null) {
                        nextEditText.requestFocus();
                    } else {
                        hideKeyboard(currentEditText);
                    }
                } else {
                    currentEditText.setText("");
                }
            }
            checkValidateButtonStatus();
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

//    private class DiceKeyListener implements View.OnKeyListener {
//        private EditText nextEditText;
//
//        public DiceKeyListener(EditText nextEditText) {
//            this.nextEditText = nextEditText;
//        }
//
//        @Override
//        public boolean onKey(View v, int keyCode, KeyEvent event) {
//            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                if (nextEditText != null) {
//                    nextEditText.requestFocus();
//                } else {
//                    hideKeyboard(v);
//                }
//                return true;
//            }
//            return false;
//        }
//    }

}
