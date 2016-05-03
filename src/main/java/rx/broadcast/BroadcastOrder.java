package rx.broadcast;

import java.util.function.Consumer;

/**
 * An order for broadcast message receipt.
 * <p>
 * Note: if the message in transit is not wrapped with any metadata (i.e. the message in transit is identical to the
 * message delivered) the type parameters should be the same.
 *
 * @param <M> the type of the message in transit
 * @param <T> the type of the message as delivered
 */
@SuppressWarnings("WeakerAccess")
public interface BroadcastOrder<M, T> {
    /**
     * Prepare the message for sending.
     *
     * @param message the message
     * @return the message as it will be sent
     */
    M prepare(T message);

    /**
     * Handle receipt of the given message. No guarantees are made about the delivery of the message; the message may be
     * held for an indeterminate amount of time and/or not delivered at all.
     *
     * @param sender the identifier of the sender
     * @param consumer the intended consumer of the message
     * @param message the message sent by the sender
     */
    void receive(long sender, Consumer<T> consumer, M message);
}
