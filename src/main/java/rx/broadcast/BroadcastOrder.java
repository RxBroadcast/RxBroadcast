package rx.broadcast;

import java.util.function.Consumer;

@FunctionalInterface
public interface BroadcastOrder<T> {
    void receive(int sender, Consumer<T> consumer, Timestamped<T> message);
}
