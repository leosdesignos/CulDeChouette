package io.github.leosdesignos.culdechouette;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.github.leosdesignos.culdechouette.game.Game;
import io.github.leosdesignos.culdechouette.game.GameEvent;
import io.github.leosdesignos.culdechouette.game.Player;
import io.github.leosdesignos.culdechouette.game.Turn;
import io.github.leosdesignos.culdechouette.game.TurnRecord;

import java.util.ArrayList;
import java.util.List;

/** The game turn by turn, latest first, with the scores and tokens of each player after every turn. */
public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        Game game = GameStore.gameOrRestart(this);
        if (game == null) {
            return;
        }

        ListView summaryListView = findViewById(R.id.summaryListView);
        TextView header = (TextView) getLayoutInflater().inflate(R.layout.summary_item, summaryListView, false);
        SpannableStringBuilder standings = new SpannableStringBuilder();
        appendTitle(standings, getString(R.string.summary_standings));
        appendStandings(standings, game.players());
        header.setText(standings);
        summaryListView.addHeaderView(header, null, false);

        List<CharSequence> turns = new ArrayList<>();
        Turn turn = game.turn();
        if (!game.isOver() && !turn.events().isEmpty()) {
            SpannableStringBuilder current = new SpannableStringBuilder();
            appendTitle(current, getString(R.string.summary_current_turn,
                    turn.number(), game.player(turn.owner()).name()));
            appendEvents(current, game, turn.events());
            turns.add(current);
        }
        List<TurnRecord> history = game.history();
        for (int i = history.size() - 1; i >= 0; i--) {
            TurnRecord record = history.get(i);
            SpannableStringBuilder text = new SpannableStringBuilder();
            appendTitle(text, getString(R.string.summary_turn, record.number(), game.player(record.owner()).name()));
            appendEvents(text, game, record.events());
            text.append('\n').append(getString(R.string.summary_after));
            appendStandings(text, record.standings());
            turns.add(text);
        }
        if (turns.isEmpty()) {
            turns.add(getString(R.string.summary_empty));
        }
        summaryListView.setAdapter(new ArrayAdapter<>(this, R.layout.summary_item, turns));
    }

    private static void appendTitle(SpannableStringBuilder text, String title) {
        int start = text.length();
        text.append(title);
        text.setSpan(new StyleSpan(Typeface.BOLD), start, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void appendEvents(SpannableStringBuilder text, Game game, List<GameEvent> events) {
        for (GameEvent event : events) {
            text.append("\n• ").append(Texts.event(this, game, event));
        }
    }

    private void appendStandings(SpannableStringBuilder text, List<Player> players) {
        for (Player player : players) {
            text.append("\n").append(Texts.standing(this, player));
        }
    }
}
