package rx.broadcast;

class VectorTimestampEntry {
    final long id;

    final long timestamp;

    VectorTimestampEntry(final long id, final long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }
}
