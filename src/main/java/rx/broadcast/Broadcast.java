package rx.broadcast;

import rx.Observable;

public interface Broadcast {
    Observable<Void> send(Object value);

    <T> Observable<T> valuesOfType(Class<T> clazz);
}
