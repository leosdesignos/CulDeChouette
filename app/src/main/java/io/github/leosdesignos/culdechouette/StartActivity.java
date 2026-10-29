package io.github.leosdesignos.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import io.github.leosdesignos.culdechouette.game.Game;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    /** Names to fill the list of players with, from the previous game. */
    static final String EXTRA_NAMES = "names";

    private static final String STATE_NAMES = "names";

    private EditText playerNameEditText;
    private ArrayAdapter<String> playersAdapter;
    private ArrayList<String> playersList;
    private Button startGameButton;
    private GameStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        store = GameStore.get(this);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        ListView playersListView = findViewById(R.id.playersListView);
        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        Button resumeButton = findViewById(R.id.resumeButton);
        startGameButton = findViewById(R.id.startGameButton);

        ArrayList<String> names = savedInstanceState != null
                ? savedInstanceState.getStringArrayList(STATE_NAMES)
                : getIntent().getStringArrayListExtra(EXTRA_NAMES);
        playersList = names != null ? names : new ArrayList<>();
        playersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersList);
        playersListView.setAdapter(playersAdapter);
        playersListView.setOnItemLongClickListener((parent, view, position, id) -> {
            removePlayer(position);
            return true;
        });

        addPlayerButton.setOnClickListener(v -> addPlayer());
        playerNameEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addPlayer();
                return true;
            }
            return false;
        });
        startGameButton.setOnClickListener(v -> startGame());
        resumeButton.setOnClickListener(v -> openGame());

        Game game = store.game();
        resumeButton.setVisibility(game != null && !game.isOver() ? View.VISIBLE : View.GONE);
        checkStartGameButton();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(STATE_NAMES, playersList);
    }

    private void addPlayer() {
        String playerName = playerNameEditText.getText().toString().trim();
        if (playerName.isEmpty()) {
            Ui.toast(this, R.string.player_name_empty);
            return;
        }
        for (String name : playersList) {
            if (name.equalsIgnoreCase(playerName)) {
                Ui.toast(this, R.string.player_name_taken, name);
                return;
            }
        }
        playersList.add(playerName);
        playersAdapter.notifyDataSetChanged();
        playerNameEditText.setText("");
        checkStartGameButton();
    }

    private void removePlayer(int position) {
        String name = playersList.remove(position);
        playersAdapter.notifyDataSetChanged();
        Ui.toast(this, R.string.player_removed, name);
        checkStartGameButton();
    }

    private void startGame() {
        Game game = store.game();
        if (game == null || game.isOver()) {
            createGame();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game)
                .setMessage(R.string.replace_game_message)
                .setPositiveButton(R.string.new_game, (dialog, which) -> createGame())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void createGame() {
        store.newGame(playersList);
        openGame();
    }

    private void openGame() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void checkStartGameButton() {
        startGameButton.setEnabled(playersList.size() >= 2);
    }
}
