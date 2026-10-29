package io.github.leosdesignos.culdechouette.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/** Saves and restores a whole game, for persistence and undo. */
public final class GameSerializer {

    private GameSerializer() { }

    public static void write(Game game, OutputStream out) throws IOException {
        ObjectOutputStream objects = new ObjectOutputStream(out);
        objects.writeObject(game);
        objects.flush();
    }

    public static Game read(InputStream in) throws IOException {
        try {
            return (Game) new ObjectInputStream(in).readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Not a saved game", e);
        }
    }

    public static byte[] toBytes(Game game) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            write(game, bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    public static Game fromBytes(byte[] bytes) {
        try {
            return read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
