package io.github.leosdesignos.culdechouette;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.leosdesignos.culdechouette.game.Game;

import java.util.Arrays;

/** The roller of a 4-2-1 can challenge another player to make it too, in three throws at most. */
public class SouffletteActivity extends AppCompatActivity {

    private GameStore store;
    private Ui.PlayerSpinner playerSpinner;
    private Spinner resultSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soufflette);
        store = GameStore.get(this);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }
        if (!game.canSoufflette()) {
            finish();
            return;
        }

        int roller = game.roller();
        Spinner spinner = findViewById(R.id.playerSpinner);
        resultSpinner = findViewById(R.id.resultSpinner);
        TextView targetText = findViewById(R.id.targetText);
        LinearLayout souffletteLayout = findViewById(R.id.souffletteLayout);
        ((TextView) findViewById(R.id.challengerText))
                .setText(getString(R.string.challenges, game.player(roller).name()));

        playerSpinner = new Ui.PlayerSpinner(spinner, game, game.otherPlayers(roller), true);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int target = playerSpinner.selected();
                souffletteLayout.setVisibility(target != Game.NOBODY ? View.VISIBLE : View.GONE);
                if (target != Game.NOBODY) {
                    targetText.setText(store.game().player(target).name());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // The position is the number of throws needed, 0 when the challenged player failed
        resultSpinner.setAdapter(Ui.adapter(this, Arrays.asList(
                getString(R.string.soufflette_failed),
                getString(R.string.soufflette_first),
                getString(R.string.soufflette_second),
                getString(R.string.soufflette_third))));

        findViewById(R.id.validateButton).setOnClickListener(v -> validate());
        if (savedInstanceState == null) {
            spinner.post(spinner::performClick);
        }
    }

    private void validate() {
        if (isFinishing()) {
            return;
        }
        int target = playerSpinner.selected();
        if (target != Game.NOBODY) {
            int throwsNeeded = resultSpinner.getSelectedItemPosition();
            store.tryRun(game -> game.soufflette(target, throwsNeeded));
        }
        finish();
    }
}
