package rx.broadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Sender implements Serializable, Comparable<Sender> {
    private static final long serialVersionUID = 114L;

    private ByteBuffer bytes;

    Sender(final byte[] bytes) {
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
        return "Sender{id=" + Arrays.toString(bytes.array()) + '}';
    }
}
