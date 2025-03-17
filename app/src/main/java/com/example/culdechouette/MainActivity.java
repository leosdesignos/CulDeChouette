package com.example.culdechouette;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView currentPlayerText;
    private DiceEditText dice1EditText;
    private DiceEditText dice2EditText;
    private DiceEditText dice3EditText;
    private TextView resultText;
    private TextView figureText;
    private TextView playersScoreText;
    private Button validateRollButton;
    private Button siroterButton;
    private Button soufletteButton;
    private Button nextTurnButton;
    private Button civetButton;
    private EditText civetBet;
    private Spinner civetFigure;
    private LinearLayout civetLayout;

    private Roll roll;
    private GameData game;

    ActivityResultLauncher<Intent> subActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        int id = intent != null ? intent.getIntExtra("id", -1) : -1;
                        switch (id) {
                            case SiroterActivity.SUBACT_ID:
                                siroterButton.setEnabled(false);
                                if (intent.getBooleanExtra("success", false)) {
                                    roll = new Roll(Roll.Figure.CUL_DE_CHOUETTE_SIROTE, roll.figureValue());
                                    updateFigureScore();
                                }
                                break;
                            case SoufletteActivity.SUBACT_ID:
                                soufletteButton.setEnabled(false);
                                break;
                            case ChouetteVeluteActivity.SUBACT_ID:
                            case SuiteActivity.SUBACT_ID:
                            default:
                                break;
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
        validateRollButton = findViewById(R.id.validateRollButton);
        siroterButton = findViewById(R.id.siroterButton);
        soufletteButton = findViewById(R.id.soufletteButton);
        nextTurnButton = findViewById(R.id.nextTurnButton);
        civetButton = findViewById(R.id.civetButton);
        civetBet = findViewById(R.id.civetBet);
        civetFigure = findViewById(R.id.civetFigure);
        civetLayout = findViewById(R.id.civetLayout);

        dice1EditText.jumpTo(dice2EditText);
        dice2EditText.jumpTo(dice3EditText);

        game = GameData.getInstance();
        currentPlayerText.setText(game.currentPlayer().name());

        validateRollButton.setOnClickListener(v -> validateRoll());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        soufletteButton.setOnClickListener(v -> showSouflettePopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());
        civetButton.setOnClickListener(v -> displayCivet());

        dice1EditText.focus();
    }

    private void validateRoll() {
        if (dice1EditText.isEmpty() || dice2EditText.isEmpty() || dice3EditText.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
            return;
        }
        validateRollButton.setEnabled(false);
        civetButton.setEnabled(false);
        civetBet.setEnabled(false);
        civetFigure.setEnabled(false);

        int dice1 = dice1EditText.value();
        int dice2 = dice2EditText.value();
        int dice3 = dice3EditText.value();

        roll = new Roll(dice1, dice2, dice3);
        updateFigureScore();

        Player currentPlayer = game.currentPlayer();
        switch (roll.figure()) {
            case CHOUETTE:
                game.addRoundScore(currentPlayer, roll.figureScore());
                siroterButton.setVisibility(View.VISIBLE);
                break;
            case CHOUETTE_VELUTE:
                showChouetteVelutePopup();
                break;
            case CUL_DE_CHOUETTE:
            case CUL_DE_CHOUETTE_SIROTE:
            case VELUTE:
                game.addRoundScore(currentPlayer, roll.figureScore());
                break;
            case SUITE_VELUTE:
                game.addRoundScore(currentPlayer, roll.figureScore());
            case SUITE:
                showSuitePopup();
                break;
            case SOUFLETTE:
                soufletteButton.setVisibility(View.VISIBLE);
                break;
            case NEANT:
                if (currentPlayer.setGrelottine(true)) {
                    Toast.makeText(getApplicationContext(), R.string.grelottine_acq, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }

        nextTurnButton.setEnabled(true);
    }

    private void nextTurn() {
        checkCivet();
        game.endRound();
        updatePlayersScore();

        // Réinitialiser les champs de dés et le score
        dice1EditText.setText("");
        dice2EditText.setText("");
        dice3EditText.setText("");
        resultText.setText(R.string.zero);
        figureText.setText(R.string.none_e);
        nextTurnButton.setEnabled(false);
        validateRollButton.setEnabled(true);
        siroterButton.setEnabled(true);
        siroterButton.setVisibility(View.GONE);
        soufletteButton.setEnabled(true);
        soufletteButton.setVisibility(View.GONE);
        civetButton.setEnabled(true);
        civetLayout.setVisibility(View.GONE);
        currentPlayerText.setText(game.currentPlayer().name());
        dice1EditText.focus();
    }

    private void updateFigureScore() {
        figureText.setText(roll.figureName());
        resultText.setText(String.valueOf(roll.figureScore()));
    }

    private void updatePlayersScore() {
        StringBuilder scoresText = new StringBuilder();
        for (Player player : game.playerList()) {
            scoresText.append(player.name()).append(" : ").append(player.score()).append("\n");
        }
        playersScoreText.setText(scoresText.toString());
    }

    private void showSiroterPopup() {
        Intent intent = new Intent(this, SiroterActivity.class);
        intent.putExtra("chouetteValue", roll.figureValue());
        subActivity.launch(intent);
    }

    private void showChouetteVelutePopup() {
        Intent intent = new Intent(this, ChouetteVeluteActivity.class);
        intent.putExtra("veluteValue", roll.figureValue());
        subActivity.launch(intent);
    }

    private void showSuitePopup() {
        Intent intent = new Intent(this, SuiteActivity.class);
        subActivity.launch(intent);
    }

    private void showSouflettePopup() {
        Intent intent = new Intent(this, SoufletteActivity.class);
        subActivity.launch(intent);
    }

    private void displayCivet() {
        if (civetLayout.getVisibility() == View.VISIBLE) {
            civetLayout.setVisibility(View.GONE);
            return;
        }
        civetBet.setText("");
        civetBet.setHint("≤" + Math.max(0, Math.min(game.currentPlayer().score(), 102)));
        civetBet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    int maxBet = Math.max(0, Math.min(game.currentPlayer().score(), 102));
                    if (Integer.parseInt(input) > maxBet) {
                        s.replace(0, s.length(), String.valueOf(maxBet));
                    }
                }
            }
        });

        List<Roll.Figure> figs = Arrays.asList(Roll.Figure.CHOUETTE, Roll.Figure.VELUTE,
                Roll.Figure.CHOUETTE_VELUTE, Roll.Figure.SUITE,
                Roll.Figure.CUL_DE_CHOUETTE, Roll.Figure.CUL_DE_CHOUETTE_SIROTE);
        ArrayAdapter<Roll.Figure> spinnerAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_spinner_item, figs);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        civetFigure.setAdapter(spinnerAdapter);

        civetBet.setEnabled(true);
        civetFigure.setEnabled(true);
        civetLayout.setVisibility(View.VISIBLE);
    }

    private void checkCivet() {
        String civetBetStr = civetBet.getText().toString();
        if (civetBetStr.isEmpty() || Integer.parseInt(civetBetStr) == 0) {
            return;
        }
        Player player = game.currentPlayer();
        if (!player.civet()) {
            game.bevue(player);
            Toast.makeText(getApplicationContext(), R.string.bevue_civet, Toast.LENGTH_SHORT).show();
        } else {
            player.setCivet(false);
            int civetBetValue = Integer.parseInt(civetBetStr);
            boolean success = roll.figure().equals(civetFigure.getSelectedItem());
            game.addRoundScore(player, success ? civetBetValue : -civetBetValue);
        }
    }
}
