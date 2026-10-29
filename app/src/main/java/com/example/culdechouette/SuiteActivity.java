package com.example.culdechouette;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.culdechouette.game.Game;

import java.util.ArrayList;
import java.util.List;

/** Designates the last player who shouted "Grelotte ça picote !". */
public class SuiteActivity extends AppCompatActivity {

    /** After ties, the loser loses 20 points, then 30... */
    private static final int MAX_MULTIPLIER = 9;

    private GameStore store;
    private Ui.PlayerSpinner playerSpinner;
    private Spinner pointSpinner;
    private Button validateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suite);
        store = GameStore.get(this);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }
        if (!game.turn().isSuitePending()) {
            finish();
            return;
        }

        Spinner spinner = findViewById(R.id.playerSpinner);
        pointSpinner = findViewById(R.id.pointSpinner);
        validateButton = findViewById(R.id.validateButton);

        playerSpinner = new Ui.PlayerSpinner(spinner, game, game.otherPlayers(), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                validateButton.setEnabled(playerSpinner.selected() != Game.NOBODY);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        validateButton.setEnabled(false);

        // Position + 1 is the multiplier of the 10 points lost
        List<String> points = new ArrayList<>();
        for (int multiplier = 1; multiplier <= MAX_MULTIPLIER; multiplier++) {
            points.add(getString(R.string.points_value, Game.SUITE_PENALTY * multiplier));
        }
        pointSpinner.setAdapter(Ui.adapter(this, points));

        validateButton.setOnClickListener(v -> validate());
        if (savedInstanceState == null) {
            spinner.post(spinner::performClick);
        }
    }

    private void validate() {
        int player = playerSpinner.selected();
        int multiplier = pointSpinner.getSelectedItemPosition() + 1;
        store.run(game -> game.loseSuite(player, multiplier));
        finish();
    }
}
