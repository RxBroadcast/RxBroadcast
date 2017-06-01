package rx.broadcast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class ObjectSerializer<T> implements Serializer<T> {
    @Override
    @SuppressWarnings("unchecked")
    public final T decode(final byte[] data) {
        try {
            final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            return (T) ois.readObject();
        } catch (final ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final byte[] encode(final T data) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data);
            oos.flush();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
