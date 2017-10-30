package rxbroadcast;

import rxbroadcast.time.LamportClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CausalOrder<T> implements BroadcastOrder<VectorTimestamped<T>, T> {
    private final class DelayQueueEntry {
        final Sender sender;

        final VectorTimestamped<T> message;

        private DelayQueueEntry(final Sender sender, final VectorTimestamped<T> message) {
            this.sender = sender;
            this.message = message;
        }
    }

    private final Sender me;

    private final Map<Sender, LamportClock> vt = new HashMap<>();

    private final List<DelayQueueEntry> delayQueue = new ArrayList<>();

    private long myNextIncomingTimestamp = 1L;

    public CausalOrder(final Sender me) {
        this.me = me;
        vt.put(me, new LamportClock());
    }

    @Override
    public VectorTimestamped<T> prepare(final T value) {
        return vt.get(me).tick(() -> {
            final Sender[] ids = vt.keySet().toArray(new Sender[vt.size()]);
            final long[] timestamps = new long[ids.length];
            for (int i = 0; i < ids.length; i++) {
                timestamps[i] = vt.get(ids[i]).time();
            }

            return new VectorTimestamped<>(value, new VectorTimestamp(ids, timestamps));
        });
    }

    @Override
    public void receive(final Sender sender, final Consumer<T> consumer, final VectorTimestamped<T> message) {
        final Map<Sender, Long> messageTimestamp = message.timestamp.asMap();
        messageTimestamp.keySet().forEach(k1 -> vt.putIfAbsent(k1, new LamportClock()));
        delayQueue.add(new DelayQueueEntry(sender, message));

        boolean wasMutated;
        do {
            wasMutated = false;
            final Iterator<DelayQueueEntry> iterator = delayQueue.iterator();
            while (iterator.hasNext()) {
                final DelayQueueEntry entry = iterator.next();
                final Map<Sender, Long> entryTimestamp = entry.message.timestamp.asMap();
                if (entry.sender.equals(me) && entryTimestamp.get(me) == myNextIncomingTimestamp) {
                    consumer.accept(entry.message.value);
                    myNextIncomingTimestamp++;
                    iterator.remove();
                    wasMutated = true;
                }
                if (shouldBeDelivered(entry.sender, entry.message)) {
                    consumer.accept(entry.message.value);
                    vt.forEach((k, v) -> v.set(Math.max(v.time(), entryTimestamp.getOrDefault(k, 0L))));
                    iterator.remove();
                    wasMutated = true;
                }
            }
        } while (wasMutated);
    }

    int delayQueueSize() {
        return delayQueue.size();
    }

    @SuppressWarnings("checkstyle:AvoidInlineConditionals")
    private boolean shouldBeDelivered(final Sender i, final VectorTimestamped<T> message) {
        final Map<Sender, Long> timestamp = message.timestamp.asMap();
        return timestamp.keySet().stream().allMatch((k) ->
            k.equals(i)
                ? timestamp.get(k) == (vt.get(k).time() + 1)
                : timestamp.get(k) <= (vt.get(k).time()));
    }
}
