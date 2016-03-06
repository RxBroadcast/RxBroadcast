package rx.broadcast;

import java.util.function.Consumer;

public final class BasicOrder<T> implements BroadcastOrder<T> {
    @Override
    public void receive(final long sender, final Consumer<T> consumer, final Timestamped<T> message) {
        consumer.accept(message.value);
    }
}
