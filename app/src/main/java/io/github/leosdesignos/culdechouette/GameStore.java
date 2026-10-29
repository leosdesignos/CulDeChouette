package io.github.leosdesignos.culdechouette;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AtomicFile;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import io.github.leosdesignos.culdechouette.game.Game;
import io.github.leosdesignos.culdechouette.game.GameSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;

/**
 * Holds the game in progress. It is saved after every action so it survives the app being killed,
 * and the state before each action is kept to undo it.
 */
public final class GameStore {

    public interface Action<T> {
        T apply(Game game);
    }

    public interface Command {
        void run(Game game);
    }

    private static final String TAG = "GameStore";
    private static final String FILE_NAME = "game.ser";
    private static final int MAX_UNDO = 50;

    private static GameStore instance;

    private final AtomicFile file;
    private final ArrayDeque<byte[]> undoStack = new ArrayDeque<>();
    @Nullable
    private Game game;

    private GameStore(File file) {
        this.file = new AtomicFile(file);
        this.game = load();
    }

    public static synchronized GameStore get(Context context) {
        if (instance == null) {
            instance = new GameStore(new File(context.getApplicationContext().getFilesDir(), FILE_NAME));
        }
        return instance;
    }

    /**
     * Returns the game in progress, or sends the user back to the start screen and finishes
     * {@code activity} when there is none.
     */
    @Nullable
    public static Game gameOrRestart(Activity activity) {
        Game game = get(activity).game();
        if (game == null) {
            activity.startActivity(new Intent(activity, StartActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            activity.finish();
        }
        return game;
    }

    /** Forgets the instance in memory, as if the app had been killed. */
    @VisibleForTesting
    static synchronized void reset() {
        instance = null;
    }

    @Nullable
    public Game game() {
        return game;
    }

    public void newGame(List<String> names) {
        game = new Game(names);
        undoStack.clear();
        save();
    }

    public void clear() {
        game = null;
        undoStack.clear();
        file.delete();
    }

    /** Applies an action to the game, or leaves the game untouched if the action throws. */
    public <T> T perform(Action<T> action) {
        if (game == null) {
            throw new IllegalStateException("No game in progress");
        }
        byte[] before = GameSerializer.toBytes(game);
        T result;
        try {
            result = action.apply(game);
        } catch (RuntimeException e) {
            game = GameSerializer.fromBytes(before);
            throw e;
        }
        undoStack.push(before);
        if (undoStack.size() > MAX_UNDO) {
            undoStack.removeLast();
        }
        save();
        return result;
    }

    public void run(Command command) {
        perform(game -> {
            command.run(game);
            return null;
        });
    }

    /**
     * Like {@link #perform}, for screens whose action may no longer be allowed, e.g. a screen
     * opened twice. Returns null when the game refused the action.
     */
    @Nullable
    public <T> T tryPerform(Action<T> action) {
        try {
            return perform(action);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Log.w(TAG, "Action refused", e);
            return null;
        }
    }

    /** Like {@link #run}, returns false when the game refused the action. */
    public boolean tryRun(Command command) {
        return tryPerform(game -> {
            command.run(game);
            return Boolean.TRUE;
        }) != null;
    }

    public boolean canUndo() {
        return game != null && !undoStack.isEmpty();
    }

    public void undo() {
        game = GameSerializer.fromBytes(undoStack.pop());
        save();
    }

    @Nullable
    private Game load() {
        try (FileInputStream in = file.openRead()) {
            return GameSerializer.read(in);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException | RuntimeException e) {
            // Saved by an incompatible version of the app: start over
            Log.w(TAG, "Can't restore the saved game", e);
            return null;
        }
    }

    private void save() {
        if (game == null) {
            return;
        }
        FileOutputStream out = null;
        try {
            out = file.startWrite();
            GameSerializer.write(game, out);
            file.finishWrite(out);
        } catch (IOException e) {
            Log.e(TAG, "Can't save the game", e);
            if (out != null) {
                file.failWrite(out);
            }
        }
    }
}
