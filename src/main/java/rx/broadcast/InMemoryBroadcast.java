package rx.broadcast;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * A {@link Broadcast} implementation that works in-memory.
 */
@SuppressWarnings("WeakerAccess")
public final class InMemoryBroadcast implements Broadcast {
    private final Subject<Object, Object> values;

    /**
     * Creates an instance of {@code InMemoryBroadcast}.
     */
    public InMemoryBroadcast() {
        values = PublishSubject.create().toSerialized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Void> send(final Object value) {
        return Observable.defer(() -> {
            values.onNext(value);
            return Observable.empty();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return values.ofType(clazz);
    }
}
