package com.example.culdechouette;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.culdechouette.game.Game;
import com.example.culdechouette.game.Player;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ScreensTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        store().clear();
        GameStore.reset();
    }

    @After
    public void tearDown() {
        store().clear();
        GameStore.reset();
    }

    @Test
    public void startScreenCreatesTheGame() {
        StartActivity activity = Robolectric.buildActivity(StartActivity.class).setup().get();
        EditText name = activity.findViewById(R.id.playerNameEditText);
        View add = activity.findViewById(R.id.addPlayerButton);
        View start = activity.findViewById(R.id.startGameButton);

        name.setText("Arthur");
        add.performClick();
        name.setText("  ");
        add.performClick();
        assertEquals(context.getString(R.string.player_name_empty), ShadowToast.getTextOfLatestToast());
        name.setText("arthur");
        add.performClick();
        assertEquals(context.getString(R.string.player_name_taken, "Arthur"), ShadowToast.getTextOfLatestToast());
        assertFalse(start.isEnabled());
        name.setText(" Bohort ");
        add.performClick();
        assertTrue(start.isEnabled());

        start.performClick();

        assertNextActivity(activity, MainActivity.class);
        assertTrue(activity.isFinishing());
        assertEquals(Arrays.asList("Arthur", "Bohort"), names(store().game()));
    }

    @Test
    public void gameScreenWithoutGameGoesBackToStart() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();

        assertTrue(activity.isFinishing());
        assertNextActivity(activity, StartActivity.class);
    }

    @Test
    public void playsATurnAndSurvivesRecreation() {
        newGame("Arthur", "Bohort");
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity activity = controller.get();
        assertEquals(context.getString(R.string.playing, "Arthur"), text(activity, R.id.statusText));
        assertTrue(text(activity, R.id.playersScoreText).contains("Bohort : 0 pts"));

        enterDice(activity, 3, 3, 4);
        activity.findViewById(R.id.validateRollButton).performClick();
        assertEquals(9, store().game().player(0).score());

        controller.recreate();
        activity = controller.get();
        assertFalse(activity.findViewById(R.id.validateRollButton).isEnabled());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.siroterButton).getVisibility());
        assertTrue(text(activity, R.id.playersScoreText).contains("Arthur : 9 pts (+9 ce tour)"));

        activity.findViewById(R.id.nextTurnButton).performClick();
        assertEquals(context.getString(R.string.playing, "Bohort"), text(activity, R.id.statusText));
        assertTrue(activity.findViewById(R.id.validateRollButton).isEnabled());
        assertEquals("", text(activity, R.id.dice1));
    }

    @Test
    public void gameSurvivesTheAppBeingKilled() {
        newGame("Arthur", "Bohort");
        store().run(game -> game.roll(2, 2, 4));
        GameStore.reset();

        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();

        assertFalse(activity.isFinishing());
        assertTrue(store().game().turn().isChouetteVelutePending());
        assertEquals(View.VISIBLE, activity.findViewById(R.id.chouetteVeluteButton).getVisibility());
        assertFalse(activity.findViewById(R.id.nextTurnButton).isEnabled());
    }

    @Test
    public void undoRestoresThePreviousState() {
        newGame("Arthur", "Bohort");
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        enterDice(activity, 6, 6, 1);
        activity.findViewById(R.id.validateRollButton).performClick();
        assertEquals(36, store().game().player(0).score());

        shadowOf(activity).clickMenuItem(R.id.action_undo);

        assertEquals(0, store().game().player(0).score());
        assertTrue(activity.findViewById(R.id.validateRollButton).isEnabled());
    }

    @Test
    public void chouetteVeluteMustBeClaimedBeforeTheNextTurn() {
        newGame("Arthur", "Bohort");
        MainActivity main = Robolectric.buildActivity(MainActivity.class).setup().get();
        enterDice(main, 2, 2, 4);
        main.findViewById(R.id.validateRollButton).performClick();
        assertNextActivity(main, ChouetteVeluteActivity.class);
        assertFalse(main.findViewById(R.id.nextTurnButton).isEnabled());

        ChouetteVeluteActivity activity = Robolectric.buildActivity(ChouetteVeluteActivity.class).setup().get();
        ((Spinner) activity.findViewById(R.id.playerSpinner)).setSelection(2);
        activity.findViewById(R.id.validateButton).performClick();

        assertTrue(activity.isFinishing());
        assertEquals(32, store().game().player(1).score());
        assertTrue(store().game().canEndTurn());
    }

    @Test
    public void sirotageScreenSettlesTheSirotageAndTheBets() {
        newGame("Arthur", "Bohort");
        store().run(game -> {
            game.roll(6, 6, 1);
            game.endTurn();
            game.roll(5, 5, 1);
            game.endTurn();
            game.roll(3, 3, 4);
        });
        SiroterActivity activity = Robolectric.buildActivity(SiroterActivity.class).setup().get();
        LinearLayout bets = activity.findViewById(R.id.betsLayout);
        assertEquals(1, bets.getChildCount());

        ((Spinner) bets.getChildAt(0).findViewById(R.id.choiceSpinner)).setSelection(3);
        activity.findViewById(R.id.validateBetButton).performClick();
        ((EditText) activity.findViewById(R.id.diceEditText)).setText("3");
        assertEquals(View.GONE, activity.findViewById(R.id.contreSiropLayout).getVisibility());
        activity.findViewById(R.id.validateButton).performClick();

        assertTrue(activity.isFinishing());
        assertEquals(36 + 70, store().game().player(0).score());
        assertEquals(25 + 20, store().game().player(1).score());
    }

    @Test
    public void diceOutOfRangeAreRejected() {
        newGame("Arthur", "Bohort");
        MainActivity activity = Robolectric.buildActivity(MainActivity.class).setup().get();
        EditText dice = activity.findViewById(R.id.dice1);

        dice.setText("7");

        assertEquals("", dice.getText().toString());
    }

    @Test
    public void grelottineScreenGoesOnAfterAPasseGrelotBevue() {
        newGame("Arthur", "Bohort", "Calogrenant");
        store().run(game -> {
            game.roll(1, 3, 6);
            game.endTurn();
            game.roll(1, 3, 6);
        });
        GrelottineActivity activity = Robolectric.buildActivity(GrelottineActivity.class).setup().get();

        ((Spinner) activity.findViewById(R.id.challengerSpinner)).setSelection(0);
        activity.findViewById(R.id.validateButton).performClick();
        assertEquals(View.VISIBLE, activity.findViewById(R.id.passeGrelotButton).getVisibility());
        activity.findViewById(R.id.passeGrelotButton).performClick();
        assertEquals(context.getString(R.string.bevue_passe_grelot, "Bohort"), ShadowToast.getTextOfLatestToast());
        assertEquals(View.GONE, activity.findViewById(R.id.passeGrelotButton).getVisibility());

        ((EditText) activity.findViewById(R.id.grelottineBet)).setText("0");
        activity.findViewById(R.id.startButton).performClick();

        assertTrue(activity.isFinishing());
        Game game = store().game();
        assertNotNull(game.turn().grelottine());
        assertEquals(0, game.turn().grelottine().challenger());
        assertEquals(1, game.roller());
    }

    @Test
    public void summaryShowsTheTurnsAndTokens() {
        newGame("Arthur", "Bohort");
        store().run(game -> {
            game.roll(1, 3, 6);
            game.endTurn();
            game.roll(3, 3, 4);
        });

        SummaryActivity activity = Robolectric.buildActivity(SummaryActivity.class).setup().get();

        ListView list = activity.findViewById(R.id.summaryListView);
        assertEquals(1 + 2, list.getAdapter().getCount());
        String standings = ((TextView) list.getAdapter().getView(0, null, list)).getText().toString();
        assertTrue(standings, standings.contains("Arthur : 0 pts – Grelottine"));
        String current = list.getAdapter().getItem(1).toString();
        assertTrue(current, current.contains("Bohort fait Chouette de 3 (+9)"));
        String first = list.getAdapter().getItem(2).toString();
        assertTrue(first, first.contains("Arthur gagne une Grelottine"));
        assertTrue(first, first.contains("Arthur : 0 pts – Grelottine"));
    }

    private GameStore store() {
        return GameStore.get(context);
    }

    private void newGame(String... names) {
        store().newGame(Arrays.asList(names));
    }

    private static List<String> names(Game game) {
        List<String> names = new ArrayList<>();
        for (Player player : game.players()) {
            names.add(player.name());
        }
        return names;
    }

    private static void enterDice(Activity activity, int dice1, int dice2, int dice3) {
        ((EditText) activity.findViewById(R.id.dice1)).setText(String.valueOf(dice1));
        ((EditText) activity.findViewById(R.id.dice2)).setText(String.valueOf(dice2));
        ((EditText) activity.findViewById(R.id.dice3)).setText(String.valueOf(dice3));
    }

    private static String text(Activity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }

    private static void assertNextActivity(Activity activity, Class<? extends Activity> expected) {
        Intent next = shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(expected.getName(), next.getComponent().getClassName());
    }
}
