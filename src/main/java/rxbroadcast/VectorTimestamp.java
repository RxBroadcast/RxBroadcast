package rxbroadcast;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

final class VectorTimestamp implements Comparable<VectorTimestamp>, Serializable {
    private static final long serialVersionUID = 114L;

    private int hashCode;

    @NotNull
    private final Sender[] ids;

    @NotNull
    private final long[] timestamps;

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

    @SuppressWarnings({"checkstyle:AvoidInlineConditionals"})
    @Override
    public final int compareTo(@NotNull final VectorTimestamp other) {
        final HashMap<Sender, Long> vt1 = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            vt1.put(ids[i], timestamps[i]);
        }

        final HashMap<Sender, Long> vt2 = new HashMap<>();
        for (int i = 0; i < other.ids.length; i++) {
            vt2.put(other.ids[i], other.timestamps[i]);
        }

        final Set<Sender> senders = vt1.keySet();
        final boolean allAreLessThanOrEql = senders.stream()
            .allMatch((s) -> vt1.containsKey(s) && vt2.containsKey(s) && vt1.get(s) <= vt2.get(s));
        final boolean oneExistsThatIsLess = senders.stream()
            .anyMatch((s) -> vt1.containsKey(s) && vt2.containsKey(s) && vt1.get(s) <  vt2.get(s));
        return (oneExistsThatIsLess && allAreLessThanOrEql) ? -1 : allAreLessThanOrEql ? 0 : 1;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final VectorTimestamp timestamp = (VectorTimestamp) o;
        return this.compareTo(timestamp) == 0;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = computeHashCode();
        }

        return hashCode;
    }

    private int computeHashCode() {
        return Objects.hash(new Object() {
            @Override
            public boolean equals(final Object o) {
                return this == o;
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(ids);
            }
        }, new Object() {
            @Override
            public boolean equals(final Object o) {
                return this == o;
            }

            @Override
            public int hashCode() {
                return Arrays.hashCode(timestamps);
            }
        });
    }
}
