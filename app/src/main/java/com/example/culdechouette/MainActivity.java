package com.example.culdechouette;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
    private Button nextTurnButton;

    private ArrayList<String> playersList;
    private Map<String, Integer> teamScores;
    private Map<String, String> teamNames;
    private int currentPlayerIndex = 0;

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
        nextTurnButton = findViewById(R.id.nextTurnButton);

        playersList = getIntent().getStringArrayListExtra("playersList");
        teamScores = new HashMap<>();
        teamNames = (HashMap<String, String>) getIntent().getSerializableExtra("teamNames");

        // Initialiser les scores des équipes à 0
        for (String player : playersList) {
            String teamName = teamNames.get(player);
            if (!teamScores.containsKey(teamName)) {
                teamScores.put(teamName, 0);
            }
        }

        updatePlayerTurn();

        validateScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateScore();
            }
        });

        nextTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextTurn();
            }
        });

        setupDiceEditTexts();
    }

    private void updatePlayerTurn() {
        if (currentPlayerIndex < playersList.size()) {
            String currentPlayer = playersList.get(currentPlayerIndex);
            currentPlayerText.setText("Joueur actuel: " + currentPlayer);
        }
        dice1EditText.requestFocus();
        showKeyboard(dice1EditText);
        updateTeamsScoreText();
    }

    private void validateScore() {
        int dice1 = Integer.parseInt(dice1EditText.getText().toString());
        int dice2 = Integer.parseInt(dice2EditText.getText().toString());
        int dice3 = Integer.parseInt(dice3EditText.getText().toString());

        // Logique de calcul du score et obtention du nom de la figure
        ScoreResult result = calculateScoreAndGetFigureName(dice1, dice2, dice3);
        resultText.setText("Score: " + result.score);
        figureText.setText("Figure réalisée: " + result.figureName);

        // Ajouter le score au total de l'équipe
        String currentPlayer = playersList.get(currentPlayerIndex);
        String currentTeam = teamNames.get(currentPlayer);
        int currentTeamScore = teamScores.get(currentTeam);
        teamScores.put(currentTeam, currentTeamScore + result.score);

        // Activer le bouton "Tour suivant"
        nextTurnButton.setEnabled(true);
        validateScoreButton.setEnabled(false);
    }

    private void nextTurn() {
        // Passer au joueur suivant
        currentPlayerIndex++;
        if (currentPlayerIndex >= playersList.size()) {
            currentPlayerIndex = 0; // Revenir au premier joueur si nécessaire
        }
        updatePlayerTurn();

        // Réinitialiser les champs de dés et le score
        dice1EditText.setText("");
        dice2EditText.setText("");
        dice3EditText.setText("");
        resultText.setText("Score: 0");
        figureText.setText("Figure réalisée: Aucune");
        validateScoreButton.setEnabled(false);
        nextTurnButton.setEnabled(false);
    }

    private ScoreResult calculateScoreAndGetFigureName(int dice1, int dice2, int dice3) {
        // Trier les dés pour simplifier les comparaisons
        int[] dice = {dice1, dice2, dice3};
        Arrays.sort(dice);

        // Vérifier Cul de Chouette
        if (dice[0] == dice[1] && dice[1] == dice[2]) {
            int value = dice[0];
            switch (value) {
                case 1: return new ScoreResult(50, "Cul de Chouette de " + value);
                case 2: return new ScoreResult(60, "Cul de Chouette de " + value);
                case 3: return new ScoreResult(70, "Cul de Chouette de " + value);
                case 4: return new ScoreResult(80, "Cul de Chouette de " + value);
                case 5: return new ScoreResult(90, "Cul de Chouette de " + value);
                case 6: return new ScoreResult(100, "Cul de Chouette de " + value);
            }
        }

        // Vérifier Chouette
        if (dice[0] == dice[1] || dice[1] == dice[2]) {
            int chouetteValue = dice[0] == dice[1] ? dice[0] : dice[2];
            return new ScoreResult(chouetteValue * chouetteValue, "Chouette de " + chouetteValue);
        }

        // Vérifier Velute
        if (dice[0] + dice[1] == dice[2]) {
            return new ScoreResult(2 * dice[2] * dice[2], "Velute de " + dice[2]);
        }

        // Vérifier Suite
        if (dice[2] == dice[1] + 1 && dice[1] == dice[0] + 1) {
            return new ScoreResult(-10, "Suite");
        }

        // Si aucune combinaison, c'est un Néant
        return new ScoreResult(0, "Néant");
    }

    private void updateTeamsScoreText() {
        StringBuilder scoresText = new StringBuilder("Scores des équipes:\n");
        for (Map.Entry<String, Integer> entry : teamScores.entrySet()) {
            scoresText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        teamsScoreText.setText(scoresText.toString());
    }

    private void setupDiceEditTexts() {
        dice1EditText.addTextChangedListener(new DiceTextWatcher(dice1EditText, dice2EditText));
        dice2EditText.addTextChangedListener(new DiceTextWatcher(dice2EditText, dice3EditText));
        dice3EditText.addTextChangedListener(new DiceTextWatcher(dice3EditText, null));

        dice1EditText.setOnKeyListener(new DiceKeyListener(dice2EditText));
        dice2EditText.setOnKeyListener(new DiceKeyListener(dice3EditText));
        dice3EditText.setOnKeyListener(new DiceKeyListener(null));
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

    private class DiceKeyListener implements View.OnKeyListener {
        private EditText nextEditText;

        public DiceKeyListener(EditText nextEditText) {
            this.nextEditText = nextEditText;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (nextEditText != null) {
                    nextEditText.requestFocus();
                } else {
                    hideKeyboard(v);
                }
                return true;
            }
            return false;
        }
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
}
