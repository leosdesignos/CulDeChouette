package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrelottineActivity extends AppCompatActivity {

    public static final int SUBACT_ID = 4;

    private Button validateButton;
    private Button startButton;
    private Spinner playerSpinner;
    private Spinner player2Spinner;
    private Spinner grelottineFigure;
    private EditText grelottineBet;
    private ListView playersListView;
    private LinearLayout grelottineLayout;

    private int maxBet = 0;
    private boolean passegrelot = false;
    private Player targetedPlayer;
    private Player targetingPlayer;
    private GameData game;
    private PlayersAdapter playersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grelottine);

        playersListView = findViewById(R.id.playersListView);
        validateButton = findViewById(R.id.validateButton);
        startButton = findViewById(R.id.startButton);
        playerSpinner = findViewById(R.id.playerSpinner);
        player2Spinner = findViewById(R.id.player2Spinner);
        grelottineFigure = findViewById(R.id.grelottineFigure);
        grelottineBet = findViewById(R.id.grelottineBet);
        grelottineLayout = findViewById(R.id.grelottineLayout);

        game = GameData.getInstance();

        validateButton.setOnClickListener(v -> validateTarget());
        startButton.setOnClickListener(v -> backToMainActivity());

        ArrayList<Player> playerItems = new ArrayList<>(game.playerList());
        playerItems.remove(game.currentPlayer());
        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);

        ArrayAdapter<Player> spinner2Adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, game.playerList());
        spinner2Adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        player2Spinner.setAdapter(spinner2Adapter);
        player2Spinner.setSelection(game.playerList().indexOf(game.currentPlayer()));
        player2Spinner.setEnabled(false);

        List<Roll.Figure> figs = Arrays.asList(Roll.Figure.CHOUETTE, Roll.Figure.VELUTE,
                Roll.Figure.CHOUETTE_VELUTE, Roll.Figure.SUITE,
                Roll.Figure.CUL_DE_CHOUETTE, Roll.Figure.CUL_DE_CHOUETTE_SIROTE);
        ArrayAdapter<Roll.Figure> spinner3Adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, figs);
        spinner3Adapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        grelottineFigure.setAdapter(spinner3Adapter);

        grelottineFigure.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int minScore = Math.min(targetingPlayer.score(), targetedPlayer.score());
                switch (position) {
                    case 0:
                        maxBet = (int) (0.33 * minScore);
                        break;
                    case 1:
                        maxBet = (int) (0.25 * minScore);
                        break;
                    case 2:
                        maxBet = (int) (0.08 * minScore);
                        break;
                    case 3:
                        maxBet = (int) (0.14 * minScore);
                        break;
                    case 4:
                        maxBet = (int) (0.16 * minScore);
                        break;
                    case 5:
                        maxBet = (int) (0.20 * minScore);
                        break;
                }
                grelottineBet.setHint("≤" + Math.max(0, maxBet));
                grelottineBet.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        grelottineBet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    if (Integer.parseInt(input) > maxBet) {
                        s.replace(0, s.length(), String.valueOf(maxBet));
                    }
                }
            }
        });

        playerSpinner.post(() -> playerSpinner.performClick());
    }

    private void validateTarget() {
        if (playerSpinner.getSelectedItem().equals(player2Spinner.getSelectedItem())
        || (passegrelot && targetedPlayer == player2Spinner.getSelectedItem())) {
            Toast.makeText(this, R.string.wrong_select_input, Toast.LENGTH_SHORT).show();
            return;
        }
        targetingPlayer = (Player) playerSpinner.getSelectedItem();
        targetedPlayer = (Player) player2Spinner.getSelectedItem();

        if (!targetingPlayer.grelottine()) {
            game.bevue(targetingPlayer);
            Toast.makeText(this, R.string.bevue_grelottine, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        validateButton.setText(passegrelot ? R.string.validate_challenge : R.string.passe_grelot);
        validateButton.setEnabled(!passegrelot);
        playerSpinner.setEnabled(false);
        player2Spinner.setEnabled(false);

        ArrayList<Player> otherPlayerList = new ArrayList<>(game.playerList());
        otherPlayerList.remove(targetedPlayer);
        otherPlayerList.remove(targetingPlayer);
        playersAdapter = new PlayersAdapter(otherPlayerList);
        playersListView.setAdapter(playersAdapter);
        playersListView.setVisibility(View.VISIBLE);

        grelottineLayout.setVisibility(View.VISIBLE);
        startButton.setEnabled(true);

        validateButton.setOnClickListener(v -> passeGrelot());
    }

    private void passeGrelot() {
        if (!targetedPlayer.setPasseGrelot(false)) {
            game.bevue(targetedPlayer);
            Toast.makeText(this, R.string.bevue_passe_grelot, Toast.LENGTH_SHORT).show();
            validateButton.setEnabled(false);
            return;
        }
        if (game.playerList().size() == 2) {
            targetedPlayer.setGrelottine(false);
            targetingPlayer.setGrelottine(false);
            Toast.makeText(this, R.string.passe_grelot_cancel, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        passegrelot = true;

        grelottineLayout.setVisibility(View.GONE);
        playersListView.setVisibility(View.INVISIBLE);
        startButton.setEnabled(false);
        validateButton.setText(R.string.validate_challenge);
        validateButton.setOnClickListener(v -> validateTarget());
        player2Spinner.setEnabled(true);
        player2Spinner.performClick();
    }

    private void backToMainActivity() {
        String grelottineBetStr = grelottineBet.getText().toString();
        if (grelottineBetStr.isEmpty()) {
            Toast.makeText(this, R.string.wrong_bet_input, Toast.LENGTH_SHORT).show();
            return;
        }
        targetedPlayer.setGrelottine(false);
        targetingPlayer.setGrelottine(false);
        game.grelottine().start(
                targetedPlayer,
                targetingPlayer,
                playersAdapter.getPlayersBet(),
                (Roll.Figure) grelottineFigure.getSelectedItem(),
                Integer.parseInt(grelottineBetStr));

        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class PlayersAdapter extends ArrayAdapter<Player> {

        private final Map<Player, Boolean> playerSelection;
        private final ArrayAdapter<CharSequence> spinnerAdapter;

        public PlayersAdapter(ArrayList<Player> playersList) {
            super(GrelottineActivity.this, R.layout.player_item, playersList);
            this.playerSelection = new HashMap<>();
            this.spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.grelottine_options, android.R.layout.simple_spinner_item);
            this.spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.player_item, parent, false);
            }

            TextView playerNameTextView = convertView.findViewById(R.id.playerNameTextView);
            Spinner betSpinner = convertView.findViewById(R.id.choiceSpinner);

            Player player = getItem(position);
            playerNameTextView.setText(player.name());

            betSpinner.setAdapter(spinnerAdapter);
            betSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        playerSelection.remove(player);
                    } else {
                        playerSelection.put(player, position == 1);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            return convertView;
        }

        public Map<Player, Boolean> getPlayersBet() {
            return playerSelection;
        }
    }
}
