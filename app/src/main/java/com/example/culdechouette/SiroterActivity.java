package com.example.culdechouette;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SiroterActivity extends AppCompatActivity {

    private TextView siroterTitle;
    private TextView siroterMessage;
    private ListView teamsListView;
    private EditText siroterEditText;
    private Button siroterButton;
    private Button validateScoreButton;

    private String currentPlayer;
    private String currentTeam;
    private ArrayList<String> playersList;
    private Map<String, String> playerTeam;
    private Map<String, Integer> teamScores;
    private Map<String, Boolean> teamCanBet;
    private Map<String, Spinner> teamSpinners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_siroter);

        siroterTitle = findViewById(R.id.siroterTitle);
        siroterMessage = findViewById(R.id.siroterMessage);
        teamsListView = findViewById(R.id.teamsListView);
        siroterEditText = findViewById(R.id.siroterEditText);
        siroterButton = findViewById(R.id.siroterButton);
        validateScoreButton = findViewById(R.id.validateScoreButton);

        // Récupérer les données passées à l'activité
        currentPlayer = getIntent().getStringExtra("currentPlayer");
        currentTeam = getIntent().getStringExtra("currentTeam");
        playersList = getIntent().getStringArrayListExtra("playersList");
        playerTeam = (HashMap<String, String>) getIntent().getSerializableExtra("playerTeam");
        teamScores = (HashMap<String, Integer>) getIntent().getSerializableExtra("teamScores");

        // Initialiser la liste des équipes qui peuvent parier
        teamCanBet = new HashMap<>();
        teamSpinners = new HashMap<>();
        for (String player : playersList) {
            String teamName = playerTeam.get(player);
            teamCanBet.put(teamName, true);
        }

        // Configurer l'adapter pour la liste des équipes
        TeamsAdapter adapter = new TeamsAdapter(playersList, playerTeam);
        teamsListView.setAdapter(adapter);

        siroterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBets();
            }
        });

        validateScoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateScore();
            }
        });
    }

    private void validateBets() {
        // Récupérer les équipes sélectionnées
        for (Map.Entry<String, Spinner> entry : teamSpinners.entrySet()) {
            String teamName = entry.getKey();
            Spinner spinner = entry.getValue();
            String selectedOption = (String) spinner.getSelectedItem();
            int betValue = 0;

            switch (selectedOption) {
                case "Linotte (1)":
                    betValue = 1;
                    break;
                case "Alouette (2)":
                    betValue = 4;
                    break;
                case "Fauvette (3)":
                    betValue = 9;
                    break;
                case "Mouette (4)":
                    betValue = 16;
                    break;
                case "Bergeronnette (5)":
                    betValue = 25;
                    break;
                case "Chouette (6)":
                    betValue = 36;
                    break;
                case "Ne pari pas":
                    betValue = 0;
                    break;
            }

            teamCanBet.put(teamName, betValue != 0);
        }

        // Afficher le champ de saisie du dé et activer le bouton de validation
        siroterEditText.setVisibility(View.VISIBLE);
        validateScoreButton.setVisibility(View.VISIBLE);
        siroterButton.setEnabled(false);
    }

    private void validateScore() {
        int betValue = Integer.parseInt(siroterEditText.getText().toString());
        int scoreResult = 0;

        // Vérifier si le dé est égal aux deux autres dés de la chouette obtenue par l’équipe qui veut siroter
        for (Map.Entry<String, Boolean> entry : teamCanBet.entrySet()) {
            String teamName = entry.getKey();
            if (entry.getValue()) {
                if (betValue == 1 || betValue == 6) {
                    scoreResult += betValue * betValue;
                } else {
                    scoreResult -= betValue;
                }
            }
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("siroterResult", scoreResult);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private class TeamsAdapter extends ArrayAdapter<String> {
        private ArrayList<String> playersList;
        private Map<String, String> playerTeam;

        public TeamsAdapter(ArrayList<String> playersList, Map<String, String> playerTeam) {
            super(SiroterActivity.this, R.layout.team_item, playersList);
            this.playersList = playersList;
            this.playerTeam = playerTeam;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.team_item, parent, false);
            }

            TextView teamNameTextView = convertView.findViewById(R.id.teamNameTextView);
            Spinner siroterSpinner = convertView.findViewById(R.id.siroterSpinner);

            String playerName = getItem(position);
            String teamName = playerTeam.get(playerName);
            teamNameTextView.setText(teamName);

            ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.dice_options, android.R.layout.simple_spinner_item);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            siroterSpinner.setAdapter(spinnerAdapter);

            teamSpinners.put(teamName, siroterSpinner);

            return convertView;
        }
    }
}
