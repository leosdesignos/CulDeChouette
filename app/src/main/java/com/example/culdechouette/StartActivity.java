package com.example.culdechouette;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private EditText playerNameEditText;
    private ListView playersListView;
    private ArrayAdapter playersAdapter;
    private ArrayList<String> playersList;
    private Button doubletteButton;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        playersListView = findViewById(R.id.playersListView);
        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        startGameButton = findViewById(R.id.startGameButton);
        doubletteButton = findViewById(R.id.doubletteButton);

        playersList = new ArrayList<>();
        playersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playersList);
        playersListView.setAdapter(playersAdapter);

        addPlayerButton.setOnClickListener(v -> addPlayer());
        doubletteButton.setOnClickListener(v -> showDoubletteHint());
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
    private void showDoubletteHint() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Doublette");
        builder.setMessage("Si vous êtes nombreux (plus de 6), il est possible de jouer en Doublette (ou Triplette !). " +
                "Deux joueurs se mettent ensemble afin de marquer des points plus rapidement. Les deux " +
                "joueurs en doublette doivent se positionner à l'opposé l'un de l'autre autour de la table.\n" +
                "Entrez alors les noms d'équipe au lieu des noms des joueurs.");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void startGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putStringArrayListExtra("playerNameList", playersList);
        startActivity(intent);
    }

    private void checkStartGameButton() {
        startGameButton.setEnabled(playersList.size() >= 2);
    }
}
