package rxbroadcast;

import org.jetbrains.annotations.NotNull;
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
    @NotNull
    @Override
    public Observable<Void> send(@NotNull final Object value) {
        return Observable.defer(() -> {
            values.onNext(value);
            return Observable.empty();
        });
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<@NotNull T> valuesOfType(@NotNull final Class<T> clazz) {
        return values.ofType(clazz);
    }
}
