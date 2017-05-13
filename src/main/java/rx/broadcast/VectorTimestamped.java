package rx.broadcast;

/**
 * Represents a value of type {@code T} that has been timestamped with a {@code VectorTimestamp}.
 * @param <T> the type of the timestamped value.
 */
@SuppressWarnings("WeakerAccess")
final class VectorTimestamped<T> {
    public VectorTimestamp timestamp;

    public T value;

    @SuppressWarnings("unused")
    VectorTimestamped() {

    }

    /**
     * Creates an instance of {@code VectorTimestamped} that associates
     * the given vector {@code timestamp} with the given {@code value}.
     * @param value the timestamped value
     * @param timestamp the timestamp
     */
    public VectorTimestamped(final T value, final VectorTimestamp timestamp) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
