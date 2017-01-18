package rx.broadcast;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public final class InMemoryBroadcast implements Broadcast {
    private final Subject<Object, Object> values;

    public InMemoryBroadcast() {
        values = PublishSubject.create().toSerialized();
    }

    @Override
    public Observable<Void> send(final Object value) {
        return Observable.defer(() -> {
            values.onNext(value);
            return Observable.empty();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Observable<T> valuesOfType(final Class<T> clazz) {
        return values.ofType(clazz);
    }
}
