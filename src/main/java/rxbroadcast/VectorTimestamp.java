package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class VectorTimestamp implements Serializable {
    private static final long serialVersionUID = 114L;

    private int hashCode;

    @NotNull
    private final Sender[] ids;

    @NotNull
    private final long[] timestamps;

    @Deprecated
    @SuppressWarnings("unused")
    VectorTimestamp() {
        this(new Sender[0], new long[0]);
    }

    VectorTimestamp(@NotNull final Sender[] ids, @NotNull final long[] timestamps) {
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

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final VectorTimestamp timestamp = (VectorTimestamp) o;
        return asMap().equals(timestamp.asMap());
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = computeHashCode();
        }

        return hashCode;
    }

    @SuppressWarnings("checkstyle:EqualsHashCode")
    private int computeHashCode() {
        return Objects.hash(new Object() {
            @Override
            public int hashCode() {
                return Arrays.hashCode(ids);
            }
        }, new Object() {
            @Override
            public int hashCode() {
                return Arrays.hashCode(timestamps);
            }
        });
    }

    Map<Sender, Long> asMap() {
        final Map<Sender, Long> map = new HashMap<>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            map.put(ids[i], timestamps[i]);
        }

        return map;
    }
}
