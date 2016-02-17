package rx.broadcast;

import rx.Observable;

public interface Broadcast {
    Observable<Void> send(final Object value);

    <T> Observable<T> valuesOfType(final Class<T> clazz);
}
