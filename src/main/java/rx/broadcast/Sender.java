package rx.broadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;

public final class Sender implements Externalizable, Comparable<Sender> {
    private static final long serialVersionUID = 114L;

    private ByteBuffer bytes;

    @Deprecated
    public Sender() {
        // This method should not be used
    }

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
        return "Sender{" + "id=" + bytes + '}';
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final byte[] bytes = this.bytes.array();
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int bufferSize = in.readInt();
        final byte[] bytes = new byte[bufferSize];
        final int readSize = in.read(bytes);
        if (readSize != bufferSize) {
            throw new IllegalStateException(
                "The given ObjectInput does not contain the correct number of bytes for this Sender instance");
        }

        this.bytes = ByteBuffer.wrap(bytes);
    }
}
