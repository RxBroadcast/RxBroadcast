package rxbroadcast;

import org.jetbrains.annotations.Contract;

import java.util.function.Consumer;

/**
 * A basic ordering of broadcasted values. {@code NoOrder} provides no guarantees of message order atop
 * what is implemented in the broadcast implementation.
 * @param <T> the type of the message
 */
public final class NoOrder<T> implements BroadcastOrder<T, T> {
    /**
     * {@inheritDoc}
     */
    @Contract(pure = true)
    @Override
    public T prepare(final T value) {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(final Sender sender, final Consumer<T> consumer, final T message) {
        consumer.accept(message);
    }
}
