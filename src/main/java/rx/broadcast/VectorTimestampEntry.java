package rx.broadcast;

final class VectorTimestampEntry {
    final long id;

    final long timestamp;

    VectorTimestampEntry(final long id, final long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }
}
