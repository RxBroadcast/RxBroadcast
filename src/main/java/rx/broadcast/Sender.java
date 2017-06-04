package rx.broadcast;

import java.nio.ByteBuffer;

public final class Sender implements Comparable<Sender> {
    private final ByteBuffer bytes;

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
    public int compareTo(final Sender o) {
        return bytes.compareTo(o.bytes);
    }

    @Override
    public String toString() {
        return "Sender{" + "id=" + bytes + '}';
    }
}
