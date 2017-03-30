package rx.broadcast;

import java.util.function.Consumer;

/**
 * A basic ordering of broadcasted values. {@code BasicOrder} provides no guarantees of message order atop
 * what is implemented in the broadcast implementation.
 * @param <T> the type of the message
 */
public final class BasicOrder<T> implements BroadcastOrder<T, T> {
    /**
     * {@inheritDoc}
     */
    @Override
    public T prepare(final T value) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(final long sender, final Consumer<T> consumer, final T message) {
        consumer.accept(message);
    }
}
