package rx.broadcast;

import rx.Observable;

/**
 * A {@code Broadcast} represents both a producer and subscriber of messages.
 */
public interface Broadcast {
    /**
     * Broadcasts the given value when the returned {@code Observable} is subscribed to.
     * @param value the value to broadcast
     * @return an Observable stream representing the message sent status
     */
    Observable<Void> send(Object value);

    /**
     * Returns a (hot) {@code Observable} stream of broadcasted values.
     * @param clazz the class type to filter the broadcasted values by
     * @param <T> the output stream type
     * @return an Observable stream of broadcasted values
     */
    <T> Observable<T> valuesOfType(Class<T> clazz);
}
