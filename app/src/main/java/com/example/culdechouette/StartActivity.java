package com.example.culdechouette;

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
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    private EditText playerNameEditText;
    private ListView playersListView;
    private PlayersAdapter playersAdapter;
    private ArrayList<String> playersList;
    private Map<String, String> teamNames;
    private Button startGameButton;
    private ToggleButton teamToggleButton;
    private View teamsLayout;
    private EditText teamNameEditText;
    private ListView teamsListView;
    private ArrayAdapter<String> teamsAdapter;
    private ArrayList<String> teamsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        playerNameEditText = findViewById(R.id.playerNameEditText);
        playersListView = findViewById(R.id.playersListView);
        Button addPlayerButton = findViewById(R.id.addPlayerButton);
        startGameButton = findViewById(R.id.startGameButton);
        teamToggleButton = findViewById(R.id.teamToggleButton);
        teamsLayout = findViewById(R.id.teamsLayout);
        teamNameEditText = findViewById(R.id.teamNameEditText);
        teamsListView = findViewById(R.id.teamsListView);
        Button addTeamButton = findViewById(R.id.addTeamButton);

        playersList = new ArrayList<>();
        teamNames = new HashMap<>();
        playersAdapter = new PlayersAdapter(playersList);
        playersListView.setAdapter(playersAdapter);

        teamsList = new ArrayList<>();
        teamsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, teamsList);
        teamsListView.setAdapter(teamsAdapter);

        addPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPlayer();
            }
        });

        teamToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                teamsLayout.setVisibility(View.VISIBLE);
                playersAdapter.showSpinners(true);
            } else {
                teamsLayout.setVisibility(View.GONE);
                playersAdapter.showSpinners(false);
            }
        });

        addTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTeam();
            }
        });

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });
    }

    private void addPlayer() {
        String playerName = playerNameEditText.getText().toString();
        if (!playerName.isEmpty()) {
            playersList.add(playerName);
            teamNames.put(playerName, playerName); // Par défaut, le nom de l'équipe est le nom du joueur
            playersAdapter.notifyDataSetChanged();
            playerNameEditText.setText("");
            checkStartGameButton();
        }
    }

    private void addTeam() {
        String teamName = teamNameEditText.getText().toString();
        if (!teamName.isEmpty()) {
            teamsList.add(teamName);
            teamsAdapter.notifyDataSetChanged();
            teamNameEditText.setText("");
            playersAdapter.updateTeams();
        }
    }

    private void startGame() {
        for (Map.Entry<String, Spinner> entry : playersAdapter.getPlayerSpinners().entrySet()) {
            String playerName = entry.getKey();
            Spinner spinner = entry.getValue();
            String selectedTeam = (String) spinner.getSelectedItem();
            if (!"Aucune équipe".equals(selectedTeam)) {
                teamNames.put(playerName, selectedTeam);
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putStringArrayListExtra("playersList", playersList);
        intent.putExtra("teamNames", (HashMap<String, String>) teamNames);
        startActivity(intent);
    }

    private void checkStartGameButton() {
        startGameButton.setEnabled(playersList.size() >= 2);
    }

    private class PlayersAdapter extends ArrayAdapter<String> {
        private Map<String, Spinner> playerSpinners;

        public PlayersAdapter(ArrayList<String> playersList) {
            super(StartActivity.this, R.layout.player_item, playersList);
            this.playerSpinners = new HashMap<>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_item, parent, false);
            }

            TextView playerNameTextView = convertView.findViewById(R.id.playerNameTextView);
            Spinner teamSpinner = convertView.findViewById(R.id.teamSpinner);

            String playerName = getItem(position);
            playerNameTextView.setText(playerName);

            // Ajouter une option par défaut si la liste des équipes est vide
            ArrayList<String> spinnerItems = new ArrayList<>(teamsList);
            if (spinnerItems.isEmpty()) {
                spinnerItems.add("Aucune équipe");
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinnerItems);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            teamSpinner.setAdapter(spinnerAdapter);

            playerSpinners.put(playerName, teamSpinner);

            if (teamToggleButton.isChecked()) {
                teamSpinner.setVisibility(View.VISIBLE);
            } else {
                teamSpinner.setVisibility(View.GONE);
            }

            return convertView;
        }

        public void showSpinners(boolean show) {
            for (Spinner spinner : playerSpinners.values()) {
                spinner.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        public void updateTeams() {
            for (Spinner spinner : playerSpinners.values()) {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
                adapter.clear();
                adapter.addAll(teamsList);
                if (adapter.getCount() == 0) {
                    adapter.add("Aucune équipe");
                }
                adapter.notifyDataSetChanged();
            }
        }

        public Map<String, Spinner> getPlayerSpinners() {
            return playerSpinners;
        }
    }
}
