package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SuiteActivity extends AppCompatActivity {

    public static final int SUBACT_ID = 2;

    private Spinner playerSpinner;
    private Spinner pointSpinner;
    private Button backButton;

    private GameData game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suite);

        playerSpinner = findViewById(R.id.playerSpinner);
        pointSpinner = findViewById(R.id.pointSpinner);
        backButton = findViewById(R.id.backButton);

        game = GameData.getInstance();

        backButton.setEnabled(false);
        backButton.setOnClickListener(v -> backToMainActivity());

        ArrayList<Player> playerItems = new ArrayList<>(game.playerList());
        playerItems.add(0, new Player(getString(R.string.no_one)));

        ArrayAdapter<Player> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, playerItems);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        playerSpinner.setAdapter(spinnerAdapter);
        playerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                backButton.setEnabled(position != 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        ArrayAdapter<CharSequence> spinnerAdapter2 = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.suite_loose_options, android.R.layout.simple_spinner_item);
        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_list_item_activated_1);
        pointSpinner.setAdapter(spinnerAdapter2);
    }

    private void backToMainActivity() {
        Player player = (Player) playerSpinner.getSelectedItem();
        if (!player.name().equals(getString(R.string.no_one))) {
            game.addRoundScore(player, -10 * (1 + pointSpinner.getSelectedItemPosition()));
        }
        Intent resultIntent = new Intent();
        resultIntent.putExtra("id", SUBACT_ID);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
