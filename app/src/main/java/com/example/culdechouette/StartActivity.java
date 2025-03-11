package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class StartActivity extends AppCompatActivity {

    private EditText playerNameEditText;
    private ArrayAdapter<String> playersAdapter;
    private ArrayList<String> playersList;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        ListView playersListView = findViewById(R.id.playersListView);
        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        startGameButton = findViewById(R.id.startGameButton);

        playersList = new ArrayList<>();
        playersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersList);
        playersListView.setAdapter(playersAdapter);

        addPlayerButton.setOnClickListener(v -> addPlayer());
        startGameButton.setOnClickListener(v -> startGame());
    }

    private void addPlayer() {
        String playerName = playerNameEditText.getText().toString();
        if (!playerName.isEmpty()) {
            playersList.add(playerName);
            playersAdapter.notifyDataSetChanged();
            playerNameEditText.setText("");
            checkStartGameButton();
        }
    }

    private void startGame() {
        GameData gameData = GameData.getInstance();
        for (String playerName : playersList) {
            gameData.playerList().add(new Player(playerName));
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void checkStartGameButton() {
        startGameButton.setEnabled(playersList.size() >= 2);
    }
}
