package rx.broadcast;

import java.util.function.Consumer;

@FunctionalInterface
public interface BroadcastOrder<T> {
    void receive(final int sender, final Consumer<T> consumer, final Timestamped<T> message);
}
