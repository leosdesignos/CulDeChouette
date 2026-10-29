package com.example.culdechouette;

import android.os.Bundle;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.culdechouette.game.Game;

/** Designates the first player who shouted "Pas mou le caillou !", who wins the Chouette Velute. */
public class ChouetteVeluteActivity extends AppCompatActivity {

    private GameStore store;
    private Ui.PlayerSpinner playerSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chouette_velute);
        store = GameStore.get(this);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }
        if (!game.turn().isChouetteVelutePending()) {
            finish();
            return;
        }

        Spinner spinner = findViewById(R.id.playerSpinner);
        playerSpinner = new Ui.PlayerSpinner(spinner, game, game.otherPlayers(), true);
        findViewById(R.id.validateButton).setOnClickListener(v -> validate());
        if (savedInstanceState == null) {
            spinner.post(spinner::performClick);
        }
    }

    private void validate() {
        int player = playerSpinner.selected();
        store.run(game -> game.claimChouetteVelute(player));
        finish();
    }
}
