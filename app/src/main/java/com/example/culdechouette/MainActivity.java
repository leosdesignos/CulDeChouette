package com.example.culdechouette;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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
    private Button nextTurnButton;

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
                                break;
                            case ChouetteVeluteActivity.SUBACT_ID:
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
        nextTurnButton = findViewById(R.id.nextTurnButton);

        dice1EditText.jumpTo(dice2EditText);
        dice2EditText.jumpTo(dice3EditText);

        game = GameData.getInstance();
        currentPlayerText.setText(game.currentPlayer().name());

        validateRollButton.setOnClickListener(v -> validateRoll());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());
    }

    private void validateRoll() {
        if (dice1EditText.isEmpty() || dice2EditText.isEmpty() || dice3EditText.isEmpty()) {
            Toast.makeText(getApplicationContext(), R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
            return;
        }
        validateRollButton.setEnabled(false);

        int dice1 = dice1EditText.value();
        int dice2 = dice2EditText.value();
        int dice3 = dice3EditText.value();

        roll = new Roll(dice1, dice2, dice3);
        figureText.setText(roll.figureName());
        resultText.setText(String.valueOf(roll.figureScore()));

        Player currentPlayer = game.currentPlayer();
        switch (roll.figure()) {
            case CHOUETTE:
                game.roundScore().put(currentPlayer, roll.figureScore());
                siroterButton.setVisibility(View.VISIBLE);
                break;
            case CHOUETTE_VELUTE:
                showChouetteVelutePopup();
                break;
            case CUL_DE_CHOUETTE:
            case CUL_DE_CHOUETTE_SIROTE:
            case VELUTE:
                game.roundScore().put(currentPlayer, roll.figureScore());
                break;
            case SUITE_VELUTE:
                game.roundScore().put(currentPlayer, roll.figureScore());
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
    }

    private void nextTurn() {
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

        currentPlayerText.setText(game.currentPlayer().name());
        dice1EditText.focus();
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

}
