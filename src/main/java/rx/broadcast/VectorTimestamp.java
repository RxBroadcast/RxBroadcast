package rx.broadcast;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class VectorTimestamp implements Serializable {
    private static final long serialVersionUID = 114L;

    private Sender[] ids;

    private long[] timestamps;

    @SuppressWarnings("unused")
    VectorTimestamp() {

    }

    VectorTimestamp(final Sender[] ids, final long[] timestamps) {
        if (ids.length != timestamps.length) {
            throw new IllegalArgumentException("IDs and timestamps must contain the same number of elements");
        }

        this.ids = ids;
        this.timestamps = timestamps;
    }

    Stream<VectorTimestampEntry> stream() {
        return IntStream.range(0, ids.length).mapToObj(idx -> new VectorTimestampEntry(ids[idx], timestamps[idx]));
    }

    @Override
    public String toString() {
        return Arrays.toString(stream().map(VectorTimestampEntry::toString).toArray());
    }
}
