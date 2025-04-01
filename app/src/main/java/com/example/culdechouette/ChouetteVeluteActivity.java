package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ChouetteVeluteActivity extends AppCompatActivity {

    public static final int SUBACT_ID = 1;

    private Spinner playerSpinner;

    private int veluteValue;
    private GameData game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chouette_velute);

        playerSpinner = findViewById(R.id.playerSpinner);
        Button backButton = findViewById(R.id.backButton);

        // Récupérer les données passées à l'activité
        veluteValue = getIntent().getIntExtra("veluteValue", -1);

        game = GameData.getInstance();

        backButton.setOnClickListener(v -> backToMainActivity());

        ArrayList<Player> playerItems = new ArrayList<>(game.playerList());
        playerItems.add(0, new Player(getString(R.string.no_one)));

        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);
        playerSpinner.post(() -> playerSpinner.performClick());
    }

    private void backToMainActivity() {
        Player player = (Player) playerSpinner.getSelectedItem();
        if (!player.name().equals(getString(R.string.no_one))) {
            game.addRoundScore(player, new Roll(Roll.Figure.CHOUETTE_VELUTE, veluteValue).figureScore());
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
