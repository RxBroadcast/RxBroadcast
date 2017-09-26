package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

final class Timestamped<T> implements Comparable<Timestamped<T>>, Serializable {
    private static final long serialVersionUID = 114L;

    @SuppressWarnings("WeakerAccess")
    public final long timestamp;

    public final T value;

    @Deprecated
    @SuppressWarnings("unused")
    Timestamped() {
        this(0, null);
    }

    Timestamped(final long timestamp, final T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public int compareTo(@NotNull final Timestamped<T> other) {
        return Long.compareUnsigned(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Timestamped{timestamp=%d, value=%s}", timestamp, value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Timestamped<?> timestamped = (Timestamped<?>) o;
        return timestamp == timestamped.timestamp && Objects.equals(value, timestamped.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, value);
    }
}
