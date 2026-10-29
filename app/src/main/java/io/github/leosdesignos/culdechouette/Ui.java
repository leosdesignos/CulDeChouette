package io.github.leosdesignos.culdechouette;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import io.github.leosdesignos.culdechouette.game.Game;

import java.util.ArrayList;
import java.util.List;

/** Small helpers shared by the screens. */
final class Ui {

    private Ui() { }

    interface IntSupplier {
        int get();
    }

    static ArrayAdapter<String> adapter(Context context, List<String> labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        return adapter;
    }

    static void toast(Context context, int resId, Object... args) {
        Toast.makeText(context, context.getString(resId, args), Toast.LENGTH_SHORT).show();
    }

    /** Value of a number field, 0 when empty. The fields are limited to 3 digits so this can't overflow. */
    static int intValue(EditText editText) {
        String text = editText.getText().toString();
        return text.isEmpty() ? 0 : Integer.parseInt(text);
    }

    /** Keeps the value of a number field at most {@code max}. */
    static void clamp(EditText editText, IntSupplier max) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (!text.isEmpty() && Integer.parseInt(text) > max.get()) {
                    s.replace(0, s.length(), String.valueOf(max.get()));
                }
            }
        });
    }

    /** A spinner listing some players, optionally preceded by "Personne". */
    static final class PlayerSpinner {

        private final Spinner spinner;
        private final int[] players;

        PlayerSpinner(Spinner spinner, Game game, List<Integer> candidates, boolean withNobody) {
            this.spinner = spinner;
            Context context = spinner.getContext();
            List<String> labels = new ArrayList<>();
            players = new int[candidates.size() + (withNobody ? 1 : 0)];
            int position = 0;
            if (withNobody) {
                labels.add(context.getString(R.string.no_one));
                players[position++] = Game.NOBODY;
            }
            for (int player : candidates) {
                labels.add(game.player(player).name());
                players[position++] = player;
            }
            spinner.setAdapter(adapter(context, labels));
        }

        /** The player selected, or {@link Game#NOBODY}. */
        int selected() {
            int position = spinner.getSelectedItemPosition();
            return position < 0 ? Game.NOBODY : players[position];
        }

        void select(int player) {
            for (int position = 0; position < players.length; position++) {
                if (players[position] == player) {
                    spinner.setSelection(position);
                    return;
                }
            }
        }
    }
}
