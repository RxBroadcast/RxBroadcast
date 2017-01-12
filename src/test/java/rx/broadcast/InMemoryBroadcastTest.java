package rx.broadcast;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class InMemoryBroadcastTest {
    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void valuesOfTypeDoesReceiveBroadcastValue() {
        final TestSubscriber<TestValue> subscriber = new TestSubscriber<>();
        final Broadcast broadcast = new InMemoryBroadcast();

        broadcast.valuesOfType(TestValue.class).first().subscribe(subscriber);
        broadcast.send(new TestValue(42)).toBlocking().subscribe();

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        subscriber.assertReceivedOnNext(Collections.singletonList(new TestValue(42)));
    }

    @SuppressWarnings({"checkstyle:magicnumber"})
    @Test
    public final void eachSubscriptionDoesSendTheBroadcast() {
        final TestSubscriber<Object> subscriber = new TestSubscriber<>();
        final Broadcast broadcast = new InMemoryBroadcast();

        broadcast.valuesOfType(TestValue.class).take(4).subscribe(subscriber);

        final Observable<Void> testValue = broadcast.send(new TestValue(42));
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();
        testValue.toBlocking().subscribe();

        subscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);

        subscriber.assertNoErrors();
        subscriber.assertValueCount(4);
        subscriber.assertReceivedOnNext(Arrays.asList(
            new TestValue(42), new TestValue(42), new TestValue(42), new TestValue(42)));
    }
}
