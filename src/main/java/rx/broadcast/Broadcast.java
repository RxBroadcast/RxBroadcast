package rx.broadcast;

import rx.Observable;

public interface Broadcast extends AutoCloseable {
    void await() throws InterruptedException;

    void send(final Object value);

    <T> Observable<T> valuesOfType(final Class<T> clazz);
}
