package rx.broadcast;

import rx.Observable;

public interface Broadcast {
    void send(final Object value);

    <T> Observable<T> valuesOfType(final Class<T> clazz);
}
