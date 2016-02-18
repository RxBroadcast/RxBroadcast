package rx.broadcast;

final class Timestamped<T> implements Comparable<Timestamped<T>> {
    public long timestamp;

    public T value;

    @SuppressWarnings("unused")
    public Timestamped() {

    }

    public Timestamped(final long timestamp, final T value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public int compareTo(final Timestamped<T> other) {
        return Long.compareUnsigned(timestamp, other.timestamp);
    }
}
