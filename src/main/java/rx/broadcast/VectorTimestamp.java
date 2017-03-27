package rx.broadcast;

import java.util.stream.IntStream;
import java.util.stream.Stream;

final class VectorTimestamp {
    private long[] ids;

    private long[] timestamps;

    @SuppressWarnings("unused")
    VectorTimestamp() {

    }

    VectorTimestamp(final long[] ids, final long[] timestamps) {
        if (ids.length != timestamps.length) {
            throw new IllegalArgumentException("IDs and timestamps must contain the same number of elements");
        }

        this.ids = ids;
        this.timestamps = timestamps;
    }

    Stream<VectorTimestampEntry> stream() {
        return IntStream.range(0, ids.length).mapToObj(idx -> new VectorTimestampEntry(ids[idx], timestamps[idx]));
    }

}
