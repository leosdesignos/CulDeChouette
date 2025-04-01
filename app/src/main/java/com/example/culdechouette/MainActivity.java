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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DiceEditText dice1EditText;
    private DiceEditText dice2EditText;
    private DiceEditText dice3EditText;
    private TextView currentPlayerText;
    private TextView playingText;
    private TextView resultText;
    private TextView figureText;
    private TextView playersScoreText;
    private TextView winnerText;
    private TextView rankText;
    private Button validateRollButton;
    private Button siroterButton;
    private Button soufletteButton;
    private Button nextTurnButton;
    private Button grelottineButton;
    private Button civetButton;
    private EditText civetBet;
    private Spinner civetFigure;
    private LinearLayout civetLayout;
    private LinearLayout gameLayout;
    private LinearLayout winnerLayout;
    private LinearLayout buttonLayout;

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
                            case GrelottineActivity.SUBACT_ID:
                                resetUI();
                                civetButton.setEnabled(false);
                                playingText.setText(R.string.playing_grelottine);
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

        dice1EditText = findViewById(R.id.dice1);
        dice2EditText = findViewById(R.id.dice2);
        dice3EditText = findViewById(R.id.dice3);
        currentPlayerText = findViewById(R.id.currentPlayerText);
        playingText = findViewById(R.id.playingText);
        resultText = findViewById(R.id.resultText);
        figureText = findViewById(R.id.figureText);
        playersScoreText = findViewById(R.id.playersScoreText);
        winnerText = findViewById(R.id.winnerText);
        rankText = findViewById(R.id.rankText);
        validateRollButton = findViewById(R.id.validateRollButton);
        siroterButton = findViewById(R.id.siroterButton);
        soufletteButton = findViewById(R.id.soufletteButton);
        nextTurnButton = findViewById(R.id.nextTurnButton);
        grelottineButton = findViewById(R.id.grelottineButton);
        civetButton = findViewById(R.id.civetButton);
        civetBet = findViewById(R.id.civetBet);
        civetFigure = findViewById(R.id.civetFigure);
        civetLayout = findViewById(R.id.civetLayout);
        gameLayout = findViewById(R.id.gameLayout);
        winnerLayout = findViewById(R.id.winnerLayout);
        buttonLayout = findViewById(R.id.buttonLayout);

        dice1EditText.jumpTo(dice2EditText);
        dice2EditText.jumpTo(dice3EditText);

        game = GameData.getInstance();
        currentPlayerText.setText(game.currentPlayer().name());
        playingText.setText(R.string.playing);

        validateRollButton.setOnClickListener(v -> validateRoll());
        siroterButton.setOnClickListener(v -> showSiroterPopup());
        soufletteButton.setOnClickListener(v -> showSouflettePopup());
        nextTurnButton.setOnClickListener(v -> nextTurn());
        grelottineButton.setOnClickListener(v -> showGrelottinePopup());
        civetButton.setOnClickListener(v -> displayCivet());

        dice1EditText.focus();
    }

    private void validateRoll() {
        if (dice1EditText.isEmpty() || dice2EditText.isEmpty() || dice3EditText.isEmpty()) {
            Toast.makeText(this, R.string.wrong_dice_input, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, R.string.grelottine_acq, Toast.LENGTH_SHORT).show();
                }
                grelottineButton.setEnabled(true);
                break;
            default:
                break;
        }

        nextTurnButton.setEnabled(true);
    }

    private void nextTurn() {
        checkCivet();
        checkGrelottine();
        game.endRound();
        updatePlayersScore();
        resetUI();
        checkWinner();
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

    private void resetUI() {
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
        grelottineButton.setEnabled(false);
        civetButton.setEnabled(true);
        civetLayout.setVisibility(View.GONE);
        currentPlayerText.setText(game.currentPlayer().name());
        playingText.setText(R.string.playing);
        dice1EditText.focus();
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

    private void showGrelottinePopup() {
        int worstScore = game.rankedPlayerList().get(game.playerList().size() - 1).score();
        if (worstScore < 0) {
            Toast.makeText(this, R.string.player_score_neg, Toast.LENGTH_SHORT).show();
            return;
        }
        checkCivet(); // Before current player change
        Intent intent = new Intent(this, GrelottineActivity.class);
        subActivity.launch(intent);
    }

    private void displayCivet() {
        civetBet.setText("");
        if (civetLayout.getVisibility() == View.VISIBLE) {
            civetLayout.setVisibility(View.GONE);
            return;
        }
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
                this, android.R.layout.simple_spinner_item, figs);
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
            Toast.makeText(this, R.string.bevue_civet, Toast.LENGTH_SHORT).show();
        } else {
            player.setCivet(false);
            int civetBetValue = Integer.parseInt(civetBetStr);
            boolean success = roll.figure().equals(civetFigure.getSelectedItem());
            game.addRoundScore(player, success ? civetBetValue : -civetBetValue);
        }
        civetBet.setText("");
    }

    private void checkGrelottine() {
        GameData.Grelottine grelottine = game.grelottine();
        if (!grelottine.enabled) {
            return;
        }
        boolean success = roll.figure().equals(grelottine.figure);

        game.addRoundScore(grelottine.targeted, success ? grelottine.stake : -grelottine.stake);
        game.addRoundScore(grelottine.targeting, success ? -grelottine.stake : grelottine.stake);
        for (Map.Entry<Player, Boolean> playerBet : grelottine.bets.entrySet()) {
            game.addRoundScore(playerBet.getKey(), playerBet.getValue() == success ? 10 : -5);
        }
        if (success) {
            grelottine.targeted.setPasseGrelot(true);
        }
    }

    private void checkWinner() {
        if (game.isWinner()) {
            dice1EditText.unfocus();
            dice2EditText.unfocus();
            dice3EditText.unfocus();
            gameLayout.setVisibility(View.GONE);
            buttonLayout.setVisibility(View.GONE);

            StringBuilder rank = new StringBuilder();
            ArrayList<Player> rankedPlayers = game.rankedPlayerList();
            for (Player player : rankedPlayers) {
                rank.append(String.format(Locale.FRENCH, "%s :  %d%s%n",
                        player.name(), player.score(), getString(R.string.points)));
            }
            winnerText.setText(rankedPlayers.get(0).name());
            rankText.setText(rank);
            winnerLayout.setVisibility(View.VISIBLE);
        }
    }
}
