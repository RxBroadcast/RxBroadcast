package rx.broadcast;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectSerializer<T> implements Serializer<T> {
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public final T decode(@NotNull final byte[] data) {
        try {
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            return (T) ois.readObject();
        } catch (final ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    public final byte[] encode(@NotNull final T data) {
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(data);
            oos.flush();
            return stream.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
