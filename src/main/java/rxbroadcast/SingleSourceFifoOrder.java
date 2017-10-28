package rxbroadcast;

import rxbroadcast.time.Clock;
import rxbroadcast.time.LamportClock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public final class SingleSourceFifoOrder<T> implements BroadcastOrder<Timestamped<T>, T> {
    public enum SingleSourceFifoOrderQueueOption {
        DROP,
        QUEUE,
    }

    @SuppressWarnings("WeakerAccess")
    public static final SingleSourceFifoOrderQueueOption DROP_LATE = SingleSourceFifoOrderQueueOption.DROP;

    private final Clock clock = new LamportClock();

    private final Map<Sender, SortedSet<Timestamped<T>>> pendingSets;

    private final Map<Sender, Long> expectedTimestamps;

    private final boolean dropLateMessages;

    public SingleSourceFifoOrder() {
        this(SingleSourceFifoOrderQueueOption.QUEUE);
    }

    @SuppressWarnings("WeakerAccess")
    public SingleSourceFifoOrder(final SingleSourceFifoOrderQueueOption queueOpt) {
        this.pendingSets = new HashMap<>();
        this.expectedTimestamps = new HashMap<>();
        this.dropLateMessages = queueOpt == SingleSourceFifoOrderQueueOption.DROP;
    }

    @Override
    public Timestamped<T> prepare(final T value) {
        return clock.tick(time -> new Timestamped<>(time, value));
    }

    @Override
    public void receive(final Sender sender, final Consumer<T> consumer, final Timestamped<T> value) {
        expectedTimestamps.putIfAbsent(sender, 1L);

        if (dropLateMessages) {
            if (Long.compareUnsigned(value.timestamp, expectedTimestamps.get(sender)) >= 0) {
                consumer.accept(value.value);
                expectedTimestamps.compute(sender, (k, v) -> value.timestamp + 1);
            }

            return;
        }

        final SortedSet<Timestamped<T>> pendingSet = pendingSets.computeIfAbsent(sender, k -> new TreeSet<>());

        pendingSet.add(value);

        final Iterator<Timestamped<T>> iterator = pendingSet.iterator();
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

    int queueSize() {
        return pendingSets.values().stream().mapToInt(SortedSet::size).sum();
    }
}
