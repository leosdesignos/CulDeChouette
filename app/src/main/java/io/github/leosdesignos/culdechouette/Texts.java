package io.github.leosdesignos.culdechouette;

import android.content.Context;

import io.github.leosdesignos.culdechouette.game.Figure;
import io.github.leosdesignos.culdechouette.game.Game;
import io.github.leosdesignos.culdechouette.game.GameEvent;
import io.github.leosdesignos.culdechouette.game.Player;
import io.github.leosdesignos.culdechouette.game.Roll;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Texts shown for the elements of the game. */
final class Texts {

    private Texts() { }

    static String points(int points) {
        return String.format(Locale.FRENCH, "%+d", points);
    }

    static String figure(Context context, Figure figure) {
        switch (figure) {
            case CHOUETTE:
                return context.getString(R.string.figure_chouette);
            case VELUTE:
                return context.getString(R.string.figure_velute);
            case CHOUETTE_VELUTE:
                return context.getString(R.string.figure_chouette_velute);
            case SUITE:
                return context.getString(R.string.figure_suite);
            case SUITE_VELUTE:
                return context.getString(R.string.figure_suite_velute);
            case CUL_DE_CHOUETTE:
                return context.getString(R.string.figure_cul_de_chouette);
            case CUL_DE_CHOUETTE_SIROTE:
                return context.getString(R.string.figure_cul_de_chouette_sirote);
            case SOUFFLETTE:
                return context.getString(R.string.figure_soufflette);
            case NEANT:
            default:
                return context.getString(R.string.figure_neant);
        }
    }

    static String roll(Context context, Roll roll) {
        return roll(context, roll.figure(), roll.value());
    }

    static String roll(Context context, Figure figure, int value) {
        switch (figure) {
            case CHOUETTE:
                return context.getString(R.string.roll_chouette, value);
            case VELUTE:
                return context.getString(R.string.roll_velute, value);
            case CHOUETTE_VELUTE:
                return context.getString(R.string.roll_chouette_velute, value);
            case SUITE:
                return context.getString(R.string.roll_suite, value);
            case SUITE_VELUTE:
                return context.getString(R.string.roll_suite_velute);
            case CUL_DE_CHOUETTE:
                return context.getString(R.string.roll_cul_de_chouette, value);
            case CUL_DE_CHOUETTE_SIROTE:
                return context.getString(R.string.roll_cul_de_chouette_sirote, value);
            case SOUFFLETTE:
                return context.getString(R.string.roll_soufflette);
            case NEANT:
            default:
                return context.getString(R.string.roll_neant);
        }
    }

    static List<String> figures(Context context, Figure[] figures) {
        List<String> labels = new ArrayList<>();
        for (Figure figure : figures) {
            labels.add(figure(context, figure));
        }
        return labels;
    }

    static String tokens(Context context, Player player) {
        List<String> tokens = new ArrayList<>();
        if (player.hasGrelottine()) {
            tokens.add(context.getString(R.string.token_grelottine));
        }
        if (player.hasCivet()) {
            tokens.add(context.getString(R.string.token_civet));
        }
        if (player.passeGrelot() == Player.PasseGrelot.AVAILABLE) {
            tokens.add(context.getString(R.string.token_passe_grelot));
        } else if (player.passeGrelot() == Player.PasseGrelot.USED) {
            tokens.add(context.getString(R.string.token_passe_grelot_used));
        }
        if (tokens.isEmpty()) {
            return context.getString(R.string.no_token);
        }
        StringBuilder text = new StringBuilder();
        for (String token : tokens) {
            if (text.length() > 0) {
                text.append(context.getString(R.string.token_separator));
            }
            text.append(token);
        }
        return text.toString();
    }

    static String standing(Context context, Player player) {
        if (player.isEliminated()) {
            return context.getString(R.string.standing_eliminated, player.name(), player.score());
        }
        return context.getString(R.string.standing_line, player.name(), player.score(), tokens(context, player));
    }

    static String event(Context context, Game game, GameEvent event) {
        String name = event.player() == Game.NOBODY ? "" : game.player(event.player()).name();
        String other = event.other() == Game.NOBODY ? "" : game.player(event.other()).name();
        String points = points(event.points());
        String otherPoints = points(event.otherPoints());
        Figure figure = event.figure();
        switch (event.type()) {
            case ROLL: {
                String roll = roll(context, figure, event.value());
                return event.points() == 0
                        ? context.getString(R.string.event_roll_no_points, name, roll)
                        : context.getString(R.string.event_roll, name, roll, points);
            }
            case SIROTAGE_WON:
                return context.getString(R.string.event_sirotage_won, name, event.value(), points);
            case SIROTAGE_LOST:
                return context.getString(R.string.event_sirotage_lost, name, event.value(), points);
            case SIROTAGE_BET_WON:
                return context.getString(R.string.event_sirotage_bet_won, name, bird(context, event.value()), points);
            case SIROTAGE_BET_LOST:
                return context.getString(R.string.event_sirotage_bet_lost, name, bird(context, event.value()), points);
            case CONTRE_SIROP:
                return context.getString(R.string.event_contre_sirop, name, points);
            case CHOUETTE_VELUTE_CLAIMED:
                return context.getString(R.string.event_chouette_velute_claimed, name, points);
            case CHOUETTE_VELUTE_UNCLAIMED:
                return context.getString(R.string.event_chouette_velute_unclaimed);
            case SUITE_LOST:
                return context.getString(R.string.event_suite_lost, name, points);
            case SOUFFLETTE_WON:
                return context.getString(R.string.event_soufflette_won, name,
                        context.getResources().getQuantityString(R.plurals.throw_count,event.value(), event.value()),
                        other, points, otherPoints);
            case SOUFFLETTE_LOST:
                return context.getString(R.string.event_soufflette_lost, name, other, points, otherPoints);
            case CIVET_ANNOUNCED:
                return context.getString(R.string.event_civet_announced,
                        name, event.value(), figure(context, figure));
            case CIVET_WON:
                return context.getString(R.string.event_civet_won, name, figure(context, figure), points);
            case CIVET_LOST:
                return context.getString(R.string.event_civet_lost, name, figure(context, figure), points);
            case GRELOTTINE_GAINED:
                return context.getString(R.string.event_grelottine_gained, name);
            case CIVET_GAINED:
                return context.getString(R.string.event_civet_gained, name);
            case PASSE_GRELOT_GAINED:
                return context.getString(R.string.event_passe_grelot_gained, name);
            case GRELOTTINE_CHALLENGE:
                return context.getString(R.string.event_grelottine_challenge,
                        name, other, event.value(), figure(context, figure));
            case PASSE_GRELOT_USED:
                return context.getString(R.string.event_passe_grelot_used, name, other);
            case GRELOTTINE_CANCELLED:
                return context.getString(R.string.event_grelottine_cancelled, name, other);
            case GRELOTTINE_WON:
                return context.getString(R.string.event_grelottine_won, name, other, points, otherPoints);
            case GRELOTTINE_LOST:
                return context.getString(R.string.event_grelottine_lost, name, other, points, otherPoints);
            case GRELOTTINE_BET_WON:
                return context.getString(R.string.event_grelottine_bet_won, name, points);
            case GRELOTTINE_BET_LOST:
                return context.getString(R.string.event_grelottine_bet_lost, name, points);
            case BEVUE_NO_CIVET:
                return context.getString(R.string.event_bevue_no_civet, name, points);
            case BEVUE_NO_GRELOTTINE:
                return context.getString(R.string.event_bevue_no_grelottine, name, points);
            case BEVUE_NO_PASSE_GRELOT:
                return context.getString(R.string.event_bevue_no_passe_grelot, name, points);
            case ELIMINATED:
                return context.getString(R.string.event_eliminated, name);
            case WINNER:
            default:
                return context.getString(R.string.event_winner, name);
        }
    }

    private static String bird(Context context, int die) {
        return context.getResources().getStringArray(R.array.dice_options)[die];
    }
}
