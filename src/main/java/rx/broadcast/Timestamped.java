package rx.broadcast;

final class Timestamped<T> implements Comparable<Timestamped<T>> {
    @SuppressWarnings("WeakerAccess")
    public long timestamp;

    public T value;

    @SuppressWarnings("unused")
    public Timestamped() {

    }

    Timestamped(final long timestamp, final T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public int compareTo(final Timestamped<T> other) {
        return Long.compareUnsigned(timestamp, other.timestamp);
    }

    @Override
    public String toString() {
        return String.format("Timestamped{timestamp=%d, value=%s}", timestamp, value);
    }
}
