package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a value of type {@code T} that has been timestamped with a {@code VectorTimestamp}.
 * @param <T> the type of the timestamped value.
 */
@SuppressWarnings("WeakerAccess")
public final class VectorTimestamped<T> implements Comparable<VectorTimestamped<T>>, Serializable {
    private static final long serialVersionUID = 114L;

    @NotNull
    public final VectorTimestamp timestamp;

    public final T value;

    @Deprecated
    @SuppressWarnings("unused")
    VectorTimestamped() {
        this(null, new VectorTimestamp());
    }

    /**
     * Creates an instance of {@code VectorTimestamped} that associates
     * the given vector {@code timestamp} with the given {@code value}.
     * @param value the timestamped value
     * @param timestamp the timestamp
     */
    public VectorTimestamped(final T value, @NotNull final VectorTimestamp timestamp) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format(
            "VectorTimestamped<%s>{value=%s, timestamp=%s}",
            value.getClass().getSimpleName(),
            value,
            timestamp);
    }

    @Override
    public final int compareTo(@NotNull final VectorTimestamped<T> other) {
        return this.timestamp.compareTo(other.timestamp);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final VectorTimestamped<?> that = (VectorTimestamped<?>) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(value, that.value);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(timestamp, value);
    }
}
