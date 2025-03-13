package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SoufletteActivity extends AppCompatActivity {

    public static final int SUBACT_ID = 3;

    private Spinner playerSpinner;
    private Spinner resultSpinner;
    private TextView player2Text;
    private LinearLayout soufletteLayout;

    private GameData game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_souflette);

        playerSpinner = findViewById(R.id.playerSpinner);
        resultSpinner = findViewById(R.id.resultSpinner);
        TextView playerText = findViewById(R.id.playerText);
        player2Text = findViewById(R.id.player2Text);
        soufletteLayout = findViewById(R.id.soufletteLayout);
        Button backButton = findViewById(R.id.backButton);

        game = GameData.getInstance();

        backButton.setOnClickListener(v -> backToMainActivity());

        playerText.setText(game.currentPlayer().name());

        ArrayList<Player> playerItems = new ArrayList<>(game.playerList());
        playerItems.add(0, new Player(getString(R.string.no_one)));
        playerItems.remove(game.currentPlayer());

        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);
        playerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                player2Text.setText(((Player) spinnerAdapter.getItem(position)).name());
                soufletteLayout.setVisibility(position != 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        ArrayAdapter<CharSequence> spinnerAdapter2 = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.souflette_options, android.R.layout.simple_spinner_item);
        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        resultSpinner.setAdapter(spinnerAdapter2);
    }

    private void backToMainActivity() {
        Player player = (Player) playerSpinner.getSelectedItem();
        if (!player.name().equals(getString(R.string.no_one))) {
            int score;
            Player currentPlayer = game.currentPlayer();
            //noinspection ConstantConditions
            int scoreP1 = game.roundScore().containsKey(currentPlayer) ? game.roundScore().get(currentPlayer) : 0;
            //noinspection ConstantConditions
            int scoreP2 = game.roundScore().containsKey(player) ? game.roundScore().get(player) : 0;
            if (0 == resultSpinner.getSelectedItemPosition()) {
                score = +30;
            } else {
                score = -60 + (10 * resultSpinner.getSelectedItemPosition());
            }
            scoreP1 += score;
            scoreP2 -= score;
            game.roundScore().put(currentPlayer, scoreP1);
            game.roundScore().put(player, scoreP2);
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
