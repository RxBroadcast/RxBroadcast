package rxbroadcast;

import rxbroadcast.time.LamportClock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public final class CausalOrder<T> implements BroadcastOrder<VectorTimestamped<T>, T> {
    private final Map<Sender, LamportClock> vectorClock = new HashMap<>();

    private final Map<Sender, SortedSet<VectorTimestamped<T>>> pending = new HashMap<>();

    private final LamportClock localClock;

    private final List<T> sendQueue = new LinkedList<>();

    public CausalOrder(final Sender me) {
        this.localClock = new LamportClock();
        vectorClock.put(me, this.localClock);
    }

    @Override
    public VectorTimestamped<T> prepare(final T value) {
        // We are not using the time argument here because we are updating
        // the reference that is stored in the vector clock array. When we
        // read from `vectorClock[me]` it should have the updated value.
        return localClock.tick((time) -> {
            final int size = vectorClock.size();
            final Sender[] ids = new Sender[size];
            final long[] timestamps = new long[size];

            int index = 0;
            for (final Map.Entry<Sender, LamportClock> entry : vectorClock.entrySet()) {
                ids[index] = entry.getKey();
                timestamps[index] = entry.getValue().time();
                index = index + 1;
            }
            return new VectorTimestamped<>(value, new VectorTimestamp(ids, timestamps));
        });
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
    public void receive(final Sender sender, final Consumer<T> consumer, final VectorTimestamped<T> message) {
        vectorClock.putIfAbsent(sender, new LamportClock());
        message.timestamp.stream().forEach((entry) -> vectorClock.putIfAbsent(entry.id, new LamportClock()));
        if (shouldBeDelivered(sender, message)) {
            deliver(sender, sendQueue::add, message);
            adjustClock(sender, message);
        } else {
            queueMessage(sender, message);
        }
        for (final Map.Entry<Sender, SortedSet<VectorTimestamped<T>>> pendingEntry : pending.entrySet()) {
            final Sender id = pendingEntry.getKey();
            final Iterator<VectorTimestamped<T>> iterator = pendingEntry.getValue().iterator();
            while (iterator.hasNext()) {
                final VectorTimestamped<T> timestamped = iterator.next();
                if (!shouldBeDelivered(id, timestamped)) {
                    continue;
                }
                deliver(id, sendQueue::add, timestamped);
                adjustClock(id, timestamped);
                iterator.remove();
            }
        }

        final Iterator<T> iterator = sendQueue.iterator();
        while (iterator.hasNext()) {
            consumer.accept(iterator.next());
            iterator.remove();
        }
    }

    int queueSize() {
        return pending.values().stream().mapToInt(SortedSet::size).sum();
    }

    private void deliver(final Sender sender, final Consumer<T> consumer, final VectorTimestamped<T> message) {
        consumer.accept(message.value);
        vectorClock.get(sender).tick();
        message.timestamp.stream().filter(entry -> entry.id.equals(sender)).forEach(
            entry -> vectorClock.get(entry.id).set(entry.timestamp));
    }

    private void queueMessage(final Sender sender, final VectorTimestamped<T> message) {
        pending.compute(sender, (id, queue) -> {
            if (queue == null) {
                return new TreeSet<>(Collections.singletonList(message));
            } else {
                queue.add(message);
                return queue;
            }
        });
    }

    private void adjustClock(final Sender sender, final VectorTimestamped<T> message) {
        final Map<Sender, Long> other = message.timestamp.stream().collect(
            Collectors.toMap((entry) -> entry.id, (entry) -> entry.timestamp));

        if (vectorClock.get(sender).time() <= other.get(sender)) {
            vectorClock.get(sender).set(other.get(sender));
        }

        other.forEach((k, v) -> {
            vectorClock.putIfAbsent(k, new LamportClock());
            vectorClock.computeIfPresent(k, (s, clock) -> {
                clock.set(Math.max(v, clock.time()));
                return clock;
            });
        });
    }

    private boolean shouldBeDelivered(final Sender sender, final VectorTimestamped<T> message) {
        return isNewestMessage(sender, message.timestamp.stream())
            && isInCausalOrder(sender, message.timestamp.stream());
    }

    private boolean isInCausalOrder(final Sender sender, final Stream<VectorTimestampEntry> timestampEntries) {
        return timestampEntries.filter(entry -> !entry.id.equals(sender)).allMatch(
            entry -> vectorClock.get(entry.id).time() >= entry.timestamp);
    }

    private boolean isNewestMessage(final Sender sender, final Stream<VectorTimestampEntry> timestampEntries) {
        return timestampEntries.filter(entry -> entry.id.equals(sender)).allMatch(
            entry -> vectorClock.get(entry.id).time() == (entry.timestamp - 1));
    }
}
