package rx.broadcast;

import java.util.function.Consumer;

public final class BasicOrder<T> implements BroadcastOrder<T, T> {
    @Override
    public T prepare(final T value) {
        return value;
    }

    @Override
    public void receive(final long sender, final Consumer<T> consumer, final T message) {
        consumer.accept(message);
    }
}
