package rx.broadcast;

import rx.broadcast.time.LamportClock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public final class CausalOrder<T> implements BroadcastOrder<VectorTimestamped<T>, T> {
    private final Map<Long, LamportClock> vectorClock = new HashMap<>();

    private final Map<Long, List<VectorTimestamped<T>>> pending = new HashMap<>();

    @Override
    public VectorTimestamped<T> prepare(final T value) {
        final int size = vectorClock.size();
        final long[] ids = new long[size];
        final long[] timestamps = new long[size];

        int index = 0;
        for (final Map.Entry<Long, LamportClock> entry : vectorClock.entrySet()) {
            ids[index] = entry.getKey();
            timestamps[index] = entry.getValue().time();
            index = index + 1;
        }

        return new VectorTimestamped<>(value, new VectorTimestamp(ids, timestamps));
    }

    /**
     * Handle receipt of the given message. No guarantees are made about the delivery of the message; the message
     * may be held for an indeterminate amount of time and/or not delivered at all.
     * <p>
     * Multiple iterations of the message timestamp are performed to avoid keeping track of flags, however the
     * time complexity of ordering a single message is still linear with respect to the timestamp size. The complexity
     * of draining the pending queues is a function of the size of the queues.
     *
     * @param sender the identifier of the sender
     * @param consumer the intended consumer of the message
     * @param message the message sent by the sender
     */
    @Override
    public void receive(final long sender, final Consumer<T> consumer, final VectorTimestamped<T> message) {
        message.timestamp.stream().forEach(entry -> vectorClock.computeIfAbsent(entry.id, key -> new LamportClock()));

        if (shouldBeDelivered(sender, message)) {
            deliver(sender, consumer, message);

            for (final Map.Entry<Long, List<VectorTimestamped<T>>> pendingEntry : pending.entrySet()) {
                final long id = pendingEntry.getKey();
                final Iterator<VectorTimestamped<T>> iterator = pendingEntry.getValue().iterator();
                while (iterator.hasNext()) {
                    final VectorTimestamped<T> timestamped = iterator.next();
                    if (!shouldBeDelivered(id, timestamped)) {
                        continue;
                    }

                    deliver(id, consumer, timestamped);
                    iterator.remove();
                }
            }
        } else {
            queueMessage(sender, message);
        }
    }

    int queueSize() {
        return pending.values().stream().mapToInt(List::size).sum();
    }

    private void deliver(final long sender, final Consumer<T> consumer, final VectorTimestamped<T> message) {
        consumer.accept(message.value);
        vectorClock.get(sender).tick();
        message.timestamp.stream().filter(entry -> entry.id != sender).forEach(
            entry -> vectorClock.get(entry.id).set(entry.timestamp));
    }

    private void queueMessage(final long sender, final VectorTimestamped<T> message) {
        pending.compute(sender, (id, queue) -> {
            if (queue == null) {
                return new LinkedList<>(Collections.singletonList(message));
            } else {
                queue.add(message);
                return queue;
            }
        });
    }

    private boolean shouldBeDelivered(final long sender, final VectorTimestamped<T> message) {
        return isNewestMessage(sender, message.timestamp.stream())
            && isInCausalOrder(sender, message.timestamp.stream());
    }

    private boolean isInCausalOrder(final long sender, final Stream<VectorTimestampEntry> timestampEntries) {
        return timestampEntries.filter(entry -> entry.id != sender).allMatch(
            entry -> vectorClock.get(entry.id).time() >= entry.timestamp);
    }

    private boolean isNewestMessage(final long sender, final Stream<VectorTimestampEntry> timestampEntries) {
        return timestampEntries.filter(entry -> entry.id == sender).allMatch(
            entry -> vectorClock.get(entry.id).time() == (entry.timestamp - 1));
    }
}
