package rx.broadcast;

/**
 * An entry in a vector timestamp, containing an ID of a message sender (called
 * a process or machine in some literature).
 */
final class VectorTimestampEntry {
    final Sender id;

    final long timestamp;

    /**
     * Creates an instance of {@code VectorTimestampEntry}.
     * @param id an ID
     * @param timestamp the timestamp value
     */
    VectorTimestampEntry(final Sender id, final long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    /**
     * Returns a string identifying this timestamp entry.
     * @return a string identifying this timestamp entry
     */
    @Override
    public String toString() {
        return String.format("(%s, %d)", id, timestamp);
    }
}
