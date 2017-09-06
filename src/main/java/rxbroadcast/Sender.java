package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;

public final class Sender implements Serializable, Comparable<Sender> {
    private static final long serialVersionUID = 114L;

    private static final int BYTES_IPV6_ADDRESS = 16;

    private static final int BYTES_INT_PORT = 4;

    private static final int BYTES_SENDER_BUFFER = BYTES_IPV6_ADDRESS + BYTES_INT_PORT;

    final byte[] byteBuffer;

    @SuppressWarnings("unused")
    Sender() {
        // This no-arg ctor is for deserialization purposes only
        this(new byte[0]);
    }

    Sender(final byte[] bytes) {
        this.byteBuffer = bytes;
    }

    Sender(final InetAddress address, final int port) {
        this(ByteBuffer.allocate(BYTES_SENDER_BUFFER)
            .put(address.getAddress())
            .putInt(port)
            .array());
    }

    @Override
    public final boolean equals(final Object o) {
        return (this == o || !(o == null || getClass() != o.getClass()))
            && Arrays.equals(byteBuffer, ((Sender) o).byteBuffer);
    }

    @Override
    public final int hashCode() {
        return ByteBuffer.wrap(byteBuffer).hashCode();
    }

    @Override
    public int compareTo(@NotNull final Sender o) {
        return ByteBuffer.wrap(byteBuffer).compareTo(ByteBuffer.wrap(o.byteBuffer));
    }

    @Override
    public String toString() {
        return String.format("Sender{id=%s}", Base64.getEncoder().encodeToString(byteBuffer));
    }
}
