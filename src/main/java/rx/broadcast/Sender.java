package rx.broadcast;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Base64;

public final class Sender implements Serializable, Comparable<Sender> {
    private static final long serialVersionUID = 114L;

    private final byte[] byteBuffer;

    private transient ByteBuffer bytes;

    Sender(final byte[] bytes) {
        this.byteBuffer = bytes;
        this.bytes = ByteBuffer.wrap(bytes);
    }

    @Override
    public final boolean equals(final Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && bytes.equals(((Sender) o).bytes);
    }

    @Override
    public final int hashCode() {
        return bytes.hashCode();
    }

    @Override
    public int compareTo(@NotNull final Sender o) {
        return bytes.compareTo(o.bytes);
    }

    @Override
    public String toString() {
        return "Sender{id=" + Base64.getEncoder().encodeToString(bytes.array()) + '}';
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.bytes = ByteBuffer.wrap(byteBuffer);
    }
}
