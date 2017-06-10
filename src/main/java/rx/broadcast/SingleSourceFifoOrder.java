package rx.broadcast;

import rx.broadcast.time.Clock;
import rx.broadcast.time.LamportClock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class SingleSourceFifoOrder<T> implements BroadcastOrder<Timestamped<T>, T> {
    @SuppressWarnings("WeakerAccess")
    public static final boolean DROP_LATE = true;

    private final Clock clock = new LamportClock();

    private final Map<Sender, SortedSet<Timestamped<T>>> pendingQueues;

    private final Map<Sender, Long> expectedTimestamps;

    private final boolean dropLateMessages;

    public SingleSourceFifoOrder() {
        this(!DROP_LATE);
    }

    @SuppressWarnings("WeakerAccess")
    public SingleSourceFifoOrder(final boolean dropLateMessages) {
        this.pendingQueues = new HashMap<>();
        this.expectedTimestamps = new HashMap<>();
        this.dropLateMessages = dropLateMessages;
    }

    @Override
    public Timestamped<T> prepare(final T value) {
        return clock.tick(time -> new Timestamped<>(time, value));
    }

    @Override
    public void receive(final Sender sender, final Consumer<T> consumer, final Timestamped<T> value) {
        expectedTimestamps.putIfAbsent(sender, 0L);

        if (dropLateMessages) {
            if (Long.compareUnsigned(value.timestamp, expectedTimestamps.get(sender)) >= 0) {
                consumer.accept(value.value);
                expectedTimestamps.compute(sender, (k, v) -> value.timestamp + 1);
            }

            return;
        }

        final SortedSet<Timestamped<T>> queue = pendingQueues.computeIfAbsent(sender, k -> new TreeSet<>());

        if (value.timestamp == expectedTimestamps.get(sender)) {
            consumer.accept(value.value);
            expectedTimestamps.compute(sender, (k, v) -> value.timestamp + 1);
        } else {
            queue.add(value);
        }

        final Iterator<Timestamped<T>> iterator = queue.iterator();
        while (iterator.hasNext()) {
            final Timestamped<T> tv = iterator.next();
            if (tv.timestamp < expectedTimestamps.get(sender)) {
                iterator.remove();
                continue;
            }
            if (tv.timestamp > expectedTimestamps.get(sender)) {
                break;
            }

            consumer.accept(tv.value);
            expectedTimestamps.compute(sender, (k, v) -> tv.timestamp + 1);
            iterator.remove();
        }
    }
}
