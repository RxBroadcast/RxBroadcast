package rx.broadcast;

@SuppressWarnings("WeakerAccess")
final class VectorTimestamped<T> {
    public VectorTimestamp timestamp;

    public T value;

    @SuppressWarnings("unused")
    public VectorTimestamped() {

    }

    public VectorTimestamped(final T value, final VectorTimestamp timestamp) {
        this.timestamp = timestamp;
        this.value = value;
    }
}
