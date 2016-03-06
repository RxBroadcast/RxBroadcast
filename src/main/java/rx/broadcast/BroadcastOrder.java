package rx.broadcast;

import java.util.function.Consumer;

@FunctionalInterface
public interface BroadcastOrder<T> {
    void receive(long sender, Consumer<T> consumer, Timestamped<T> message);
}
